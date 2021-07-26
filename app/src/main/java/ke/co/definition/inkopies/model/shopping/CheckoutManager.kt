package ke.co.definition.inkopies.model.shopping

import io.reactivex.Completable
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import ke.co.definition.inkopies.utils.logging.Logger
import java.util.*
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 29/05/18.
 */
interface CheckoutManager {
    fun checkout(slid: String, branchName: String?, storeName: String?, date: Date): Completable
}

class CheckoutManagerImpl @Inject constructor(
        private val auth: Authable,
        private val shoppingCl: ShoppingClient,
        private val resMan: ResourceManager,
        private val logger: Logger
) : CheckoutManager {

    override fun checkout(slid: String, branchName: String?, storeName: String?, date: Date): Completable {
        return Completable.create {
            validateCheckout(slid).subscribe({
                auth.getJWT().subscribe({ jwt ->
                    shoppingCl.checkout(jwt.value, slid, branchName, storeName, date)
                            .subscribe(it::onComplete) { ex ->
                                it.onError(handleAuthErrors(logger, auth, resMan, ex))
                            }
                }, it::onError)
            }, it::onError)
        }
    }

    private fun validateCheckout(slid: String) = Completable.create {
        if (slid.isBlank()) throw Exception("Shopping List ID was empty")
        it.onComplete()
    }
}