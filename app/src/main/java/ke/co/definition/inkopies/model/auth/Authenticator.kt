package ke.co.definition.inkopies.model.auth

import com.google.gson.Gson
import ke.co.definition.inkopies.repos.local.LocalStorable
import ke.co.definition.inkopies.repos.ms.AuthClient
import ke.co.definition.inkopies.repos.ms.STATUS_BAD_REQUEST
import ke.co.definition.inkopies.repos.ms.STATUS_CONFLICT
import ke.co.definition.inkopies.repos.ms.STATUS_SERVER_ERROR
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Observable
import rx.Single
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 28/02/18.
 */
class Authenticator @Inject constructor(
        private val localStore: LocalStorable,
        private val authCl: AuthClient,
        private val validator: Validatable
) : Authable {

    override fun updateIdentifier(identifier: String): Single<VerifLogin> {
        return Single.create<ValidationResult>({
            val vr = validator.validateIdentifier(identifier)
            if (!vr.isValid) {
                it.onError(Exception("The email/phone provided was invalid"))
                return@create
            }
            it.onSuccess(vr)
        }).flatMap { vr: ValidationResult ->
            getJWT().flatMap { jwt: JWT ->
                authCl.updateIdentifier(jwt.info.userID, jwt.value, vr.getIdentifier())
                        .onErrorResumeNext {
                            Single.error(handleNewIdentifierErrors(it,
                                    identifier, "updating identifier"))
                        }
                        .doOnSuccess { upsertAuthUser(it) }
                        .flatMap {
                            when (vr) {
                                is ValidationResult.ValidOnEmail -> Single.just(it.email)
                                is ValidationResult.ValidOnPhone -> Single.just(it.phone)
                                is ValidationResult.Invalid -> throw RuntimeException("Working with" +
                                        " invalid identifier while updating identifier for user")
                            }
                        }
            }
        }
    }

    override fun isLoggedIn(): Single<Boolean> = Single.create({

        val jwtStr = localStore.fetch(KEY_JWT)
        if (jwtStr.isEmpty()) {
            it.onSuccess(false)
            return@create
        }

        val jwt = Gson().fromJson(jwtStr, JWT::class.java)
        if (jwt.isExpired()) {
            logOut().subscribe { it.onSuccess(false) }
            return@create
        }

        it.onSuccess(true)
    })

    override fun registerManual(id: Identifier, password: String): Single<VerifLogin> =
            authCl.registerManual(id, password)
                    .onErrorResumeNext {
                        Single.error(handleNewIdentifierErrors(it, id.value(), "registering"))
                    }
                    .doOnSuccess { saveLoggedInDetails(it) }
                    .map {
                        return@map when (id) {
                            is Identifier.Email -> it.email
                            is Identifier.Phone -> it.phone
                        }
                    }

    override fun loginManual(id: Identifier, password: String): Completable =
            authCl.login(id, password)
                    .onErrorResumeNext {
                        if (it is HttpException && it.code() == 401) {
                            return@onErrorResumeNext Single.error(Exception("invalid username/password combination"))
                        }
                        return@onErrorResumeNext Single.error(handleServerErrors(it, "logging in"))
                    }
                    .doOnSuccess { saveLoggedInDetails(it) }
                    .toCompletable()

    override fun sendVerifyOTP(vl: VerifLogin): Single<OTPStatus> =
            validateVerifLogin(vl).flatMap { vr: ValidationResult ->
                getJWT().flatMap {
                    authCl.sendVerifyOTP(it.value, vr.getIdentifier()).onErrorResumeNext {
                        Single.error(handleServerErrors(it, "logging in"))
                    }
                }
            }


    override fun checkIdentifierVerified(vl: VerifLogin): Completable =
            validateVerifLogin(vl).flatMap { vr: ValidationResult ->
                getJWT().flatMap { jwt: JWT ->
                    authCl.fetchUserDetails(jwt.value, jwt.info.userID)
                            .onErrorResumeNext {
                                Single.error(handleServerErrors(it, "logging in"))
                            }
                            .doOnSuccess {
                                upsertAuthUser(it)
                                when (vr) {
                                    is ValidationResult.ValidOnEmail ->
                                        if (!it.email.verified) {
                                            throw Exception("${vl.value} not verified")
                                        }
                                    is ValidationResult.ValidOnPhone -> {
                                        if (!it.phone.verified) {
                                            throw Exception("${vl.value} not verified")
                                        }
                                    }
                                }
                            } // doOnSuccess

                } // getLoggedInUser()

            }.toCompletable()

    override fun verifyOTP(vl: VerifLogin, otp: String?): Completable {
        return Completable.create({
            if (otp == null || otp.isEmpty()) {
                it.onError(Exception("Empty verification code provided"))
                return@create
            }
            it.onCompleted()
        }).andThen(validateVerifLogin(vl).doOnSuccess {
            authCl.verifyOTP(vl.userID, it.getIdentifier().type(), otp!!).onErrorResumeNext {
                Completable.error(handleServerErrors(it, "logging in"))
            }
        }).toCompletable()
    }

    override fun resendInterval(otps: OTPStatus, intervalSecs: Long): Observable<String> {
        val now = Date().time
        val aMinFromNow = now + 60 * 1000
        val expTime = Math.min(otps.expiresAt.time, aMinFromNow)
        val tteSecs = (expTime - now) / 1000

        return Observable.interval(intervalSecs, TimeUnit.SECONDS)
                .take(tteSecs.toInt()) // no risk of integer overflow because cannot be greater than a minute
                .map { Math.abs(it - tteSecs) } // invert to count from max downwards instead of from min upwards
                // TODO extract string resource
                .map { String.format("Resend in %02d:%02d", it % 3600 / 60, it % 60) }
    }

    private fun logOut(): Completable = Completable.create({
        localStore.delete(KEY_JWT)
        localStore.delete(KEY_AUTHED_USER)
        it.onCompleted()
    })

    private fun validateVerifLogin(vl: VerifLogin): Single<ValidationResult> =
            Single.create<ValidationResult>({
                val vr = validator.validateIdentifier(vl.value)
                if (!vr.isValid) {
                    it.onError(Exception("invalid email/phone number"))
                }
                it.onSuccess(vr)
            })

    private fun getJWT(): Single<JWT> = Single.create({

        val jwtStr = localStore.fetch(KEY_JWT)
        if (jwtStr.isEmpty()) {
            it.onError(Exception("User not logged in"))
        }

        val jwt = Gson().fromJson(jwtStr, JWT::class.java)
        if (jwt.isExpired()) {
            logOut().subscribe({
                it.onError(Exception("Login has expired, log in again"))
            })
            return@create
        }

        it.onSuccess(jwt)
    })

    private fun handleNewIdentifierErrors(err: Throwable, frID: String, ctx: String): Throwable {
        if (err is HttpException) {
            when (err.code()) {
                STATUS_BAD_REQUEST -> return Exception("$frID was invalid")
                STATUS_CONFLICT -> return Exception("$frID is already in use")
            }
        }
        return handleServerErrors(err, ctx)
    }

    private fun handleServerErrors(err: Throwable, ctx: String = ""): Throwable {
        if (err is HttpException && err.code() >= STATUS_SERVER_ERROR) {
            // TODO log WARN with error and ctx
            return Exception("Something wicked happened, please try again", err)
        }
        if (err is IOException) {
            // TODO log WARN with error and ctx
            return Exception("Couldn't reach server, please try again")
        }
        // TODO log ERROR with error and ctx
        throw Exception("Something wicked happened, please try again")
    }

    private fun saveLoggedInDetails(usr: AuthUser) {
        upsertAuthUser(usr)
        val jwt = JWT(usr.token)
        localStore.upsert(KEY_JWT, Gson().toJson(jwt))
    }

    private fun upsertAuthUser(usr: AuthUser) {
        localStore.upsert(KEY_AUTHED_USER, Gson().toJson(usr))
    }

    companion object {
        val KEY_AUTHED_USER = Authenticator::class.java.name + "KEY_AUTHED_USER"
        val KEY_JWT = Authenticator::class.java.name + "KEY_JWT"
    }

}
