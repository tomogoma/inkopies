package ke.co.definition.inkopies.repos.ms.users

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import ke.co.definition.inkopies.model.user.GenUserProfile
import ke.co.definition.inkopies.model.user.Gender
import ke.co.definition.inkopies.model.user.PubUserProfile
import java.util.*

/**
 * Created by tomogoma
 * On 19/03/18.
 */
interface UsersClient {
    fun getPubUser(userID: String): Single<PubUserProfile>
    fun getUser(token: String, userID: String): Single<GenUserProfile>
    fun updateUser(token: String, userID: String, name: String, gender: Gender): Single<GenUserProfile>
    fun updateAvatar(token: String, userID: String, newURI: String): Single<GenUserProfile>
}

data class MSUserProfile(
        @SerializedName("ID")
        val id: String,
        val name: String?,
        @SerializedName("ICEPhone")
        val icePhone: String?,
        val gender: Gender?,
        val avatarURL: String?,
        val bio: String?,
        val rating: Float?,
        val created: Date,
        val lastUpdated: Date
) {

    fun toGenUserProfile() = GenUserProfile(
            name ?: "",
            gender ?: Gender.NONE,
            avatarURL ?: ""
    )
}

data class MSPubUserProfile(
        @SerializedName("ID")
        val id: String,
        val name: String?,
        val avatarURL: String?
) {

    fun toPubUserProfile() = PubUserProfile(
            id,
            name ?: "",
            avatarURL ?: ""
    )
}