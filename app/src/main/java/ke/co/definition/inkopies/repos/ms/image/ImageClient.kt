package ke.co.definition.inkopies.repos.ms.image

import android.net.Uri
import io.reactivex.Single

/**
 * Created by tomogoma
 * On 19/03/18.
 */
interface ImageClient {
    fun uploadProfilePic(token: String, folder: String, uri: Uri): Single<String>
}