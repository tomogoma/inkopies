package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.model.auth.OTPStatus
import ke.co.definition.inkopies.utils.injection.AuthModule
import okhttp3.Credentials
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

    private val authAPI by lazy { retrofit.create(AuthAPI::class.java) }

    override fun sendVerifyOTP(token: String, id: Identifier): Single<OTPStatus> =
            authAPI.sendVerifyOTP(id.type(), token, IdentifierRequest(id.value()))

    override fun login(id: Identifier, secret: String): Single<AuthUser> =
            authAPI.login(id.type(), Credentials.basic(id.value(), secret))

    override fun fetchUserDetails(token: String, userID: String): Single<AuthUser> =
            authAPI.fetchUserDetails(userID, token)

    override fun verifyOTP(userID: String, loginType: String, otp: String): Completable =
            authAPI.verifyOTP(userID, loginType, otp)

    override fun registerManual(id: Identifier, secret: String): Single<AuthUser> =
            authAPI.register(id.type(), AuthRegRequest(id.value(), secret))
}