package ke.co.definition.inkopies.repos.ms.image

import rx.Single

/**
 * Created by tomogoma
 * On 19/03/18.
 */
interface ImageClient {
    fun uploadProfilePic(token: String, folder: String, uri: String): Single<String>
}