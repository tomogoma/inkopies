package ke.co.definition.inkopies.model.auth

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.gson.Gson
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.repos.local.LocalStorable
import ke.co.definition.inkopies.repos.ms.*
import ke.co.definition.inkopies.repos.ms.auth.AuthClient
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Observable
import rx.Single
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
        private val validator: Validatable,
        private val resMan: ResourceManager
) : Authable {

    override fun updateIdentifier(identifier: String): Single<VerifLogin> {
        return Single.create<ValidationResult>({
            val vr = validator.validateIdentifier(identifier)
            if (!vr.isValid) {
                it.onError(Exception(resMan.getString(R.string.error_bad_email_or_phone)))
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
                                is ValidationResult.Invalid -> throw RuntimeException("Working " +
                                        "with invalid identifier while updating identifier for user")
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
                            return@onErrorResumeNext Single.error(
                                    Exception(resMan.getString(R.string.error_invalid_login)))
                        }
                        return@onErrorResumeNext Single.error(
                                handleServerErrors(resMan, it, "logging in"))
                    }
                    .doOnSuccess { saveLoggedInDetails(it) }
                    .toCompletable()

    override fun sendVerifyOTP(vl: VerifLogin): Single<OTPStatus> =
            validateVerifLogin(vl).flatMap { vr: ValidationResult ->
                getJWT().flatMap {
                    authCl.sendVerifyOTP(it.value, vr.getIdentifier()).onErrorResumeNext {
                        Single.error(handleServerErrors(resMan, it, "logging in"))
                    }
                }
            }


    override fun checkIdentifierVerified(vl: VerifLogin): Completable =
            validateVerifLogin(vl).flatMap { vr: ValidationResult ->
                getJWT().flatMap { jwt: JWT ->
                    authCl.fetchUserDetails(jwt.value, jwt.info.userID)
                            .onErrorResumeNext {
                                Single.error(handleServerErrors(resMan, it, "logging in"))
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
        return validateVerifLogin(vl)
                .flatMap {
                    if (otp == null || otp.isEmpty()) {
                        throw Exception("Empty verification code provided")
                    }
                    return@flatMap Single.just(it)
                }
                .flatMap {
                    authCl.verifyOTP(vl.userID, it.getIdentifier().type(), otp!!)
                            .toSingle({})
                }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_UNAUTHORIZED) {
                        return@onErrorResumeNext Single.error(Exception(
                                resMan.getString(R.string.error_invalid_or_expired_verif_code)))
                    }
                    if (it is HttpException && it.code() == STATUS_FORBIDDEN) {
                        return@onErrorResumeNext Single.error(Exception(
                                resMan.getString(R.string.error_used_verif_code)))
                    }
                    return@onErrorResumeNext Single.error(
                            handleServerErrors(resMan, it, "verifying OTP"))
                }
                .toCompletable()
    }

    override fun resendInterval(otps: OTPStatus?, intervalSecs: Long): Observable<String> {
        val now = Date().time
        val aMinFromNow = now + 60 * 1000
        val expTime = Math.min(otps?.expiresAt?.time ?: aMinFromNow, aMinFromNow)
        val tteSecs = (expTime - now) / 1000

        return Observable.interval(intervalSecs, TimeUnit.SECONDS)
                .take(tteSecs.toInt()) // no risk of integer overflow because cannot be greater than a minute
                .map { Math.abs(it - tteSecs) } // invert to count from max downwards instead of from min upwards
                // TODO extract string resource
                .map {
                    String.format("%s %02d:%02d", resMan.getString(R.string.resend_in),
                            it % 3600 / 60, it % 60)
                }
    }

    override fun glideURL(url: String): Single<GlideUrl> {
        return getJWT()
                .flatMap {
                    Single.just(GlideUrl(url, LazyHeaders.Builder()
                            .addHeader("x-api-key", API_KEY)
                            .addHeader("Authorization", bearerToken(it.value))
                            .build()))
                }
    }

    override fun getUser(): Single<AuthUser> =
            isLoggedIn().flatMap { isLoggedIn: Boolean ->

                if (!isLoggedIn) {
                    return@flatMap Single.error<AuthUser>(
                            Exception(resMan.getString(R.string.please_log_in)))
                }

                return@flatMap Single.create<AuthUser>({

                    val usrStr = localStore.fetch(KEY_AUTHED_USER)
                    if (usrStr.isEmpty()) {
                        // TODO have special error to force activity to navigate to login screen
                        it.onError(Exception(resMan.getString(R.string.please_log_in)))
                    }

                    val usr = Gson().fromJson(usrStr, AuthUser::class.java)
                    it.onSuccess(usr)
                })
            }

    override fun getJWT(): Single<JWT> = Single.create({

        val jwtStr = localStore.fetch(KEY_JWT)
        if (jwtStr.isEmpty()) {
            // TODO have special error to force activity to navigate to login screen
            it.onError(Exception(resMan.getString(R.string.please_log_in)))
        }

        val jwt = Gson().fromJson(jwtStr, JWT::class.java)
        if (jwt.isExpired()) {
            logOut().subscribe({
                // TODO have special error to force activity to navigate to login screen
                it.onError(Exception(resMan.getString(R.string.please_log_in)))
            })
            return@create
        }

        it.onSuccess(jwt)
    })

    private fun logOut(): Completable = Completable.create({
        localStore.delete(KEY_JWT)
        localStore.delete(KEY_AUTHED_USER)
        it.onCompleted()
    })

    private fun validateVerifLogin(vl: VerifLogin): Single<ValidationResult> =
            Single.create<ValidationResult>({
                val vr = validator.validateIdentifier(vl.value)
                if (!vr.isValid) {
                    it.onError(Exception(resMan.getString(R.string.error_bad_email_or_phone)))
                }
                it.onSuccess(vr)
            })

    private fun handleNewIdentifierErrors(err: Throwable, frID: String, ctx: String): Throwable {
        if (err is HttpException) {
            when (err.code()) {
                STATUS_BAD_REQUEST ->
                    return Exception(String.format(resMan.getString(R.string.ss_was_invalid), frID))
                STATUS_CONFLICT ->
                    return Exception(String.format(resMan.getString(R.string.ss_in_use), frID))
            }
        }
        return handleServerErrors(resMan, err, ctx)
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
