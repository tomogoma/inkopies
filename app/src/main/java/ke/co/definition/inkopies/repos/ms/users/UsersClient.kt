package ke.co.definition.inkopies.repos.ms.users

import com.google.gson.annotations.SerializedName
import ke.co.definition.inkopies.model.user.Gender
import rx.Single
import java.util.*

/**
 * Created by tomogoma
 * On 19/03/18.
 */
interface UsersClient {
    fun getUser(token: String, userID: String): Single<MSUserProfile>
    fun updateUser(token: String, userID: String, name: String, gender: Gender): Single<MSUserProfile>
    fun updateAvatar(token: String, userID: String, newURI: String): Single<MSUserProfile>
}

data class MSUserProfile(
        @SerializedName("ID")
        val id: String = "",
        val name: String = "",
        @SerializedName("ICEPhone")
        val icePhone: String = "",
        val gender: Gender = Gender.NONE,
        val avatarURL: String = "",
        val bio: String = "",
        val rating: Float = 0F,
        val created: Date = Date(0),
        val lastUpdated: Date = Date(0)
)