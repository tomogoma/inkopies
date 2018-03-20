package ke.co.definition.inkopies.repos.ms.auth

import com.google.gson.annotations.SerializedName
import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.model.auth.OTPStatus
import ke.co.definition.inkopies.model.auth.VerifLogin
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
                    .map { it.toAuthUser() }


    override fun sendVerifyOTP(token: String, id: Identifier): Single<OTPStatus> =
            authAPI.sendVerifyOTP(id.type(), token, IdentifierOnlyRequest(id.value()))
                    .map { it.toOTPStatus() }

    override fun login(id: Identifier, secret: String): Single<Pair<AuthUser, String>> =
            authAPI.login(id.type(), Credentials.basic(id.value(), secret))
                    .map { Pair(it.toAuthUser(), it.getToken()) }

    override fun fetchUserDetails(token: String, userID: String): Single<AuthUser> =
            authAPI.fetchUserDetails(userID, token)
                    .map { it.toAuthUser() }

    override fun verifyOTP(userID: String, loginType: String, otp: String): Completable =
            authAPI.verifyOTP(userID, loginType, otp)

    override fun registerManual(id: Identifier, secret: String): Single<Pair<AuthUser, String>> =
            authAPI.register(id.type(), AuthRegRequest(id.value(), secret))
                    .map { Pair(it.toAuthUser(), it.getToken()) }
}

interface AuthAPI {

    @PUT("{loginType}/register?selfReg=device")
    @Headers("x-api-key: $API_KEY")
    fun register(
            @Path("loginType") loginType: String,
            @Body body: AuthRegRequest
    ): Single<MSAuthUser>

    @POST("{loginType}/verify")
    @Headers("x-api-key: $API_KEY")
    fun sendVerifyOTP(
            @Path("loginType") loginType: String,
            @Query("token") token: String,
            @Body body: IdentifierOnlyRequest
    ): Single<MSOTPStatus>

    @POST("{loginType}/login")
    @Headers("x-api-key: $API_KEY")
    fun login(
            @Path("loginType") loginType: String,
            @Header("Authorization") basicAuth: String
    ): Single<MSAuthUser>

    @GET("users/{userID}")
    @Headers("x-api-key: $API_KEY")
    fun fetchUserDetails(
            @Path("userID") userID: String,
            @Query("token") token: String
    ): Single<MSAuthUser>

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
    ): Single<MSAuthUser>
}

data class MSAuthUser(
        @SerializedName("ID") private val id: String,
        @SerializedName("JWT") private val token: String?,
        private val phone: MSVerifLogin?,
        private val email: MSVerifLogin?
) {

    internal fun toAuthUser() = AuthUser(
            id,
            phone?.toVerifLogin() ?: VerifLogin(),
            email?.toVerifLogin() ?: VerifLogin()
    )

    internal fun getToken() = token ?: ""
}

data class MSVerifLogin(
        @SerializedName("ID") private val id: String,
        private val userID: String,
        private val value: String,
        private val verified: Boolean,
        @SerializedName("OTPStatus") private val otpStatus: MSOTPStatus?
) {
    internal fun toVerifLogin() = VerifLogin(id, userID, value, verified, otpStatus?.toOTPStatus())
}

data class MSOTPStatus(
        private val obfuscatedAddress: String,
        private val expiresAt: Date
) {
    internal fun toOTPStatus() = OTPStatus(obfuscatedAddress, expiresAt)
}

data class AuthRegRequest(
        private val identifier: String,
        private val secret: String,
        private val userType: String = "individual",
        private val deviceID: String = UUID.randomUUID().toString()
)

data class IdentifierOnlyRequest(private val identifier: String)

data class FullIdentifierRequest(private val loginType: String, private val identifier: String)