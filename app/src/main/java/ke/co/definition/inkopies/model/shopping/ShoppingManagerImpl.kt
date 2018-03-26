package ke.co.definition.inkopies.model.shopping

import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import ke.co.definition.inkopies.utils.logging.Logger
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Single
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class ShoppingManagerImpl @Inject constructor(
        private val client: ShoppingClient,
        private val auth: Authable,
        private val logger: Logger,
        private val resMan: ResourceManager
) : ShoppingManager {

    init {
        logger.setTag(ShoppingManagerImpl::class.java.name)
    }

    override fun updateShoppingListItem(item: ShoppingListItem): Single<ShoppingListItem> {
        return validateShoppingListItem(item)
                .toSingle {}
                .flatMap { auth.getJWT() }
                .flatMap { client.updateShoppingListItem(it.value, item) }
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, resMan, it, "update shopping list item"))
                }
    }

    override fun getShoppingListItems(shoppingListID: String, offset: Long, count: Int): Single<List<ShoppingListItem>> {
        return auth.getJWT()
                .flatMap { client.getShoppingListItems(it.value, shoppingListID, offset, count) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(mutableListOf<ShoppingListItem>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, resMan, it, "get shopping list items"))
                }
    }

    override fun getShoppingLists(offset: Long, count: Int): Single<List<ShoppingList>> {
        return auth.getJWT()
                .flatMap { client.getShoppingLists(it.value, offset, count) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(mutableListOf<ShoppingList>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, resMan, it,
                            "fetch shopping lists"))
                }
    }

    override fun createShoppingList(name: String): Single<ShoppingList> {
        return auth.getJWT()
                .flatMap { client.addShoppingList(it.value, name) }
                .onErrorResumeNext { Single.error(handleAuthErrors(logger, resMan, it, "add shopping list")) }
    }

    private fun validateShoppingListItem(item: ShoppingListItem) = Completable.create {
        if (item.itemName() == "") {
            it.onError(Exception("item name cannot be empty"))
            return@create
        }
        it.onCompleted()
    }
}