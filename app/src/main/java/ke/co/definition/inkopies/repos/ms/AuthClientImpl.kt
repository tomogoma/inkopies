package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.model.auth.OTPStatus
import ke.co.definition.inkopies.utils.injection.AuthModule
import retrofit2.Retrofit
import rx.Completable
import rx.Single
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 01/03/18.
 */
class AuthClientImpl @Inject constructor(@Named(AuthModule.NAME) private val retrofit: Retrofit) : AuthClient {

    override fun sendVerifyOTP(token: String, id: Identifier): Single<OTPStatus> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(id: Identifier, secret: String): Single<AuthUser> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchUserDetails(token: String, userID: String): Single<AuthUser> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verifyOTP(userID: String, loginType: String, otp: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerManual(id: Identifier, secret: String): Single<AuthUser> =
            retrofit.create(AuthAPI::class.java)
                    .register(id.type(), AuthRegRequest(id.value(), secret))
}