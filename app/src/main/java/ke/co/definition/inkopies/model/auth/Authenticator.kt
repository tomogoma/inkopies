package ke.co.definition.inkopies.model.auth

import com.google.gson.Gson
import ke.co.definition.inkopies.repos.LocalStorable
import ke.co.definition.inkopies.repos.ms.AuthClient
import ke.co.definition.inkopies.repos.ms.STATUS_BAD_REQUEST
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
        private val authCl: AuthClient
) : Authable {

    override fun isLoggedIn(): Single<Boolean> = Single.create({

        val loginStr = localStore.fetch(KEY_LOGIN_DETAILS)
        if (loginStr.isEmpty()) {
            it.onSuccess(false)
            return@create
        }

        val authUsr = Gson().fromJson(loginStr, AuthUser::class.java)
        if (authUsr.expiry.after(Date())) {
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
                        throw RuntimeException(it)
                    }
                    .map {
                        return@map when (id) {
                            is Identifier.Email -> it.email
                            is Identifier.Phone -> it.phone
                        }
                    }

    override fun loginManual(id: Identifier, password: String) = Completable.create({
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    })

    override fun sendVerifyOTP(id: Identifier): Single<OTPStatus> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun identifierVerified(vl: VerifLogin): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verifyOTP(vl: VerifLogin): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    companion object {
        val KEY_LOGIN_DETAILS = Authenticator::class.java.name + "KEY_LOGIN_DETAILS"
    }

}
