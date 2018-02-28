package ke.co.definition.inkopies.model.auth

import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 28/02/18.
 */
interface Authable {
    fun isLoggedIn(): Single<Boolean>
    fun registerManual(identifier: String, password: String): Completable
    fun loginManual(identifier: String, password: String): Completable
}