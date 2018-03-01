package ke.co.definition.inkopies.model.auth

import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 28/02/18.
 */
interface Authable {
    fun isLoggedIn(): Single<Boolean>
    fun registerManual(id: Identifier, password: String): Single<AuthUser>
    fun loginManual(id: Identifier, password: String): Completable
}