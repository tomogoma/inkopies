package ke.co.definition.inkopies.model.shopping

import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import rx.Completable
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
        private val resMan: ResourceManager
) : CheckoutManager {
    override fun checkout(slid: String, branchName: String?, storeName: String?, date: Date): Completable {
        return Completable.create {
            it.onError(Exception(resMan.getString(R.string.feature_not_implemented)))
        }
    }
}