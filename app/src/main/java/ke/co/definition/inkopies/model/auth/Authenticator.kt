package ke.co.definition.inkopies.model.auth

import android.util.Base64
import com.google.gson.Gson
import ke.co.definition.inkopies.repos.local.LocalStorable
import ke.co.definition.inkopies.repos.ms.AuthClient
import ke.co.definition.inkopies.repos.ms.STATUS_BAD_REQUEST
import ke.co.definition.inkopies.repos.ms.STATUS_SERVER_ERROR
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Observable
import rx.Single
import java.nio.charset.Charset
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

    override fun isLoggedIn(): Single<Boolean> = Single.create({

        val loginStr = localStore.fetch(KEY_LOGIN_DETAILS)
        if (loginStr.isEmpty()) {
            it.onSuccess(false)
            return@create
        }

        val authUsr = Gson().fromJson(loginStr, AuthUser::class.java)
        if (isTokenExpired(authUsr)) {
            localStore.delete(KEY_LOGIN_DETAILS)
            it.onSuccess(false)
            return@create
        }

        it.onSuccess(true)
    })

    override fun registerManual(id: Identifier, password: String): Single<VerifLogin> =
            authCl.registerManual(id, password)
                    .onErrorResumeNext {
                        if (it is HttpException && it.code() == STATUS_BAD_REQUEST) {
                            return@onErrorResumeNext Single.error(Exception("${id.value()} is already in use"))
                        }
                        return@onErrorResumeNext Single.error(handleServerErrors(it, "registering"))
                    }
                    .doOnSuccess { localStore.upsert(KEY_LOGIN_DETAILS, Gson().toJson(it)) }
                    .map {
                        return@map when (id) {
                            is Identifier.Email -> it.email
                            is Identifier.Phone -> it.phone
                        }
                    }

    override fun loginManual(id: Identifier, password: String): Completable =
            authCl.login(id, password)
                    .onErrorResumeNext { Single.error(handleServerErrors(it, "logging in")) }
                    .doOnSuccess { localStore.upsert(KEY_LOGIN_DETAILS, Gson().toJson(it)) }
                    .toCompletable()

    private fun handleServerErrors(err: Throwable, ctx: String = ""): Throwable {
        if (err is HttpException && err.code() >= STATUS_SERVER_ERROR) {
            return Exception("Something wicked happened, please try again", err)
        }
        throw RuntimeException(ctx, err)
    }

    override fun sendVerifyOTP(vl: VerifLogin): Single<OTPStatus> =
            validateVerifLogin(vl).flatMap { vr: ValidationResult ->
                getLoggedInUser()
                        .flatMap {
                            authCl.sendVerifyOTP(it.token, vr.getIdentifier()).onErrorResumeNext {
                                Single.error(handleServerErrors(it, "logging in"))
                            }
                        }
            }


    override fun checkIdentifierVerified(vl: VerifLogin): Completable =
            validateVerifLogin(vl).flatMap { vr: ValidationResult ->

                getLoggedInUser()
//                        TODO return need log out error .doOnError()
                        .flatMap { usr: AuthUser ->

                            authCl.fetchUserDetails(usr.token, usr.id).onErrorResumeNext {
                                Single.error(handleServerErrors(it, "logging in"))
                            }.doOnSuccess {
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
                it.onError(Exception("Empty OTP provided"))
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

    fun logOut(): Completable = Completable.create({
        localStore.delete(KEY_LOGIN_DETAILS)
        it.onCompleted()
    })

    fun validateVerifLogin(vl: VerifLogin): Single<ValidationResult> = Single.create<ValidationResult>({

        val vr = validator.validateIdentifier(vl.value)
        if (!vr.isValid) {
            it.onError(Exception("invalid email/phone number"))
        }
        it.onSuccess(vr)

    })

    fun getLoggedInUser(): Single<AuthUser> = Single.create({

        val loginStr = localStore.fetch(KEY_LOGIN_DETAILS)
        if (loginStr.isEmpty()) {
            it.onError(Exception("User not logged in"))
        }

        val authUsr = Gson().fromJson(loginStr, AuthUser::class.java)
        if (isTokenExpired(authUsr)) {
            localStore.delete(KEY_LOGIN_DETAILS)
            it.onError(Exception("Login has expired, log in again"))
            return@create
        }

        it.onSuccess(authUsr)
    })

    private fun isTokenExpired(authUsr: AuthUser): Boolean {
        val jwt = extractJWT(authUsr.token)
        return jwt.isExpired()
    }

    private fun extractJWT(token: String): JWT {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw Exception("invalid JWT: has ${parts.size} parts instead of 3")
        }
        val jwtBytes = Base64.decode(parts[1], Base64.URL_SAFE)
        val jwtStr = String(jwtBytes, Charset.defaultCharset())
        return Gson().fromJson(jwtStr, JWT::class.java)
    }

    companion object {
        val KEY_LOGIN_DETAILS = Authenticator::class.java.name + "KEY_LOGIN_DETAILS"
    }

}
