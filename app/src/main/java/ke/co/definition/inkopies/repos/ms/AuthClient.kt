package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import rx.Single

/**
 * Created by tomogoma
 * On 01/03/18.
 */
interface AuthClient {
    fun registerManual(id: Identifier, secret: String): Single<AuthUser>
}