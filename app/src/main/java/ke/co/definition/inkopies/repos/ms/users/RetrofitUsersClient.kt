package ke.co.definition.inkopies.repos.ms.users

import com.google.gson.annotations.SerializedName
import ke.co.definition.inkopies.model.user.Gender
import ke.co.definition.inkopies.model.user.PubUserProfile
import ke.co.definition.inkopies.repos.ms.API_KEY
import ke.co.definition.inkopies.repos.ms.bearerToken
import ke.co.definition.inkopies.utils.injection.UserModule
import retrofit2.Retrofit
import retrofit2.http.*
import rx.Single
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 19/03/18.
 */
class RetrofitUsersClient @Inject constructor(@Named(UserModule.MS) private val retrofit: Retrofit) : UsersClient {

    private val usersMSAPI by lazy { retrofit.create(UsersMSAPI::class.java) }

    override fun getPubUser(userID: String): Single<PubUserProfile> {
        return usersMSAPI.getPubUser(userID)
                .map { it.toPubUserProfile() }
    }

    override fun getUser(token: String, userID: String) =
            usersMSAPI.getUser(userID, bearerToken(token))
                    .map { it.toGenUserProfile() }

    override fun updateUser(token: String, userID: String, name: String, gender: Gender) =
            usersMSAPI.updateUser(bearerToken(token), userID,
                    UpdateUserRequest(name = name, gender = gender.name))
                    .map { it.toGenUserProfile() }

    override fun updateAvatar(token: String, userID: String, newURI: String) =
            usersMSAPI.updateUser(bearerToken(token), userID,
                    UpdateUserRequest(avatarURL = newURI))
                    .map { it.toGenUserProfile() }

}

interface UsersMSAPI {

    @GET("users/{userID}")
    @Headers("x-api-key: $API_KEY")
    fun getUser(
            @Path("userID") userID: String,
            @Header("Authorization") bearerToken: String? = null
    ): Single<MSUserProfile>

    @GET("users/{userID}")
    @Headers("x-api-key: $API_KEY")
    fun getPubUser(
            @Path("userID") userID: String,
            @Header("Authorization") bearerToken: String? = null
    ): Single<MSPubUserProfile>

    @PUT("users/{userID}")
    @Headers("x-api-key: $API_KEY")
    fun updateUser(
            @Header("Authorization") bearerToken: String,
            @Path("userID") userID: String,
            @Body body: UpdateUserRequest
    ): Single<MSUserProfile>
}

data class UpdateUserRequest(
        val name: String? = null,
        @SerializedName("ICEPhone")
        val icePhone: String? = null,
        val gender: String? = null,
        val avatarURL: String? = null,
        val bio: String? = null
)