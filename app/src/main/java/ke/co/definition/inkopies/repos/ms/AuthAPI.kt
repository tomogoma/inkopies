package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.OTPStatus
import retrofit2.http.*
import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 01/03/18.
 */
interface AuthAPI {

    @PUT("{loginType}/register?$KEY_SELF_REG=device")
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
            @Body body: IdentifierRequest
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
}

data class IdentifierRequest(val identifier: String)