package ke.co.definition.inkopies.model.user

import rx.Single

/**
 * Created by tomogoma
 * On 07/03/18.
 */
interface ProfileManager {
    fun getUser(): Single<UserProfile>
    fun updateGeneral(name: String, gender: Gender): Single<UserProfile>
    fun uploadProfilePic(uri: String): Single<UserProfile>
}