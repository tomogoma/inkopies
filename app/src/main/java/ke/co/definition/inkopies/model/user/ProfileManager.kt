package ke.co.definition.inkopies.model.user

import android.net.Uri
import rx.Single

/**
 * Created by tomogoma
 * On 07/03/18.
 */
interface ProfileManager {
    fun getPubUser(id: String): Single<PubUserProfile>
    fun getUser(): Single<UserProfile>
    fun updateGeneral(name: String, gender: Gender): Single<UserProfile>
    fun uploadProfilePic(uri: Uri): Single<UserProfile>
}