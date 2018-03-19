package ke.co.definition.inkopies.repos.ms.auth

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.model.auth.OTPStatus
import ke.co.definition.inkopies.repos.ms.API_KEY
import ke.co.definition.inkopies.utils.injection.AuthModule
import okhttp3.Credentials
import retrofit2.Retrofit
import retrofit2.http.*
import rx.Completable
import rx.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 01/03/18.
 */
class RetrofitAuthClient @Inject constructor(@Named(AuthModule.MS) private val retrofit: Retrofit) : AuthClient {

    private val authAPI by lazy { retrofit.create(AuthAPI::class.java) }

    override fun updateIdentifier(userID: String, token: String, id: Identifier): Single<AuthUser> =
            authAPI.updateIdentifier(userID, token, FullIdentifierRequest(id.type(), id.value()))

    override fun sendVerifyOTP(token: String, id: Identifier): Single<OTPStatus> =
            authAPI.sendVerifyOTP(id.type(), token, IdentifierOnlyRequest(id.value()))

    override fun login(id: Identifier, secret: String): Single<AuthUser> =
            authAPI.login(id.type(), Credentials.basic(id.value(), secret))

    override fun fetchUserDetails(token: String, userID: String): Single<AuthUser> =
            authAPI.fetchUserDetails(userID, token)

    override fun verifyOTP(userID: String, loginType: String, otp: String): Completable =
            authAPI.verifyOTP(userID, loginType, otp)

    override fun registerManual(id: Identifier, secret: String): Single<AuthUser> =
            authAPI.register(id.type(), AuthRegRequest(id.value(), secret))
}

interface AuthAPI {

    @PUT("{loginType}/register?selfReg=device")
    @Headers("x-api-key: $API_KEY")
    fun register(
            @Path("loginType") loginType: String,
            @Body body: AuthRegRequest
    ): Single<AuthUser>

    @POST("{loginType}/verify")
    @Headers("x-api-key: $API_KEY")
    fun sendVerifyOTP(
            @Path("loginType") loginType: String,
            @Query("token") token: String,
            @Body body: IdentifierOnlyRequest
    ): Single<OTPStatus>

    @POST("{loginType}/login")
    @Headers("x-api-key: $API_KEY")
    fun login(
            @Path("loginType") loginType: String,
            @Header("Authorization") basicAuth: String
    ): Single<AuthUser>

    @GET("users/{userID}")
    @Headers("x-api-key: $API_KEY")
    fun fetchUserDetails(
            @Path("userID") userID: String,
            @Query("token") token: String
    ): Single<AuthUser>

    @GET("users/{userID}/{loginType}/verify/{OTP}")
    @Headers("x-api-key: $API_KEY")
    fun verifyOTP(
            @Path("userID") userID: String,
            @Path("loginType") loginType: String,
            @Path("OTP") otp: String
    ): Completable

    @POST("users/{userID}")
    @Headers("x-api-key: $API_KEY")
    fun updateIdentifier(
            @Path("userID") userID: String,
            @Query("token") token: String,
            @Body body: FullIdentifierRequest
    ): Single<AuthUser>
}

data class AuthRegRequest(
        val identifier: String,
        val secret: String,
        val userType: String = "individual",
        val deviceID: String = UUID.randomUUID().toString()
)

data class IdentifierOnlyRequest(val identifier: String)

data class FullIdentifierRequest(val loginType: String, val identifier: String)