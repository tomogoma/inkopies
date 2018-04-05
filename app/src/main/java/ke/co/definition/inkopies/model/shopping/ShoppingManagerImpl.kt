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

    override fun updateShoppingList(list: ShoppingList): Single<ShoppingList> {
        return auth.getJWT()
                .flatMap { client.updateShoppingList(it.value, list) }
                .onErrorResumeNext { Single.error(handleAuthErrors(logger, auth, resMan, it, "update shopping list")) }
    }

    override fun searchShoppingListItem(req: ShoppingListItemSearch): Single<List<ShoppingListItem>> {
        return auth.getJWT()
                .flatMap { client.searchShoppingListItem(it.value, req) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(listOf<ShoppingListItem>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "search shopping list itme"))
                }
    }

    override fun insertShoppingListItem(item: ShoppingListItemInsert): Single<ShoppingListItem> {
        return validateShoppingListItemInsert(item)
                .toSingle {}
                .flatMap { auth.getJWT() }
                .flatMap { client.insertShoppingListItem(it.value, item) }
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, auth, resMan, it, "upsert shopping list item"))
                }
    }

    override fun deleteShoppingListItem(shoppingListID: String, id: String): Completable {
        var jwt = ""
        return auth.getJWT()
                .map { jwt = it.value }
                .toCompletable()
                .andThen(client.deleteShoppingListItem(jwt, shoppingListID, id))
                .onErrorResumeNext {
                    Completable.error(handleAuthErrors(logger, auth, resMan, it, "delete shopping list item"))
                }
    }

    override fun updateShoppingListItem(req: ShoppingListItemUpdate): Single<ShoppingListItem> {
        return validateShoppingListItemUpdate(req)
                .toSingle {}
                .flatMap { auth.getJWT() }
                .flatMap { client.updateShoppingListItem(it.value, req) }
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, auth, resMan, it, "update shopping list item"))
                }
    }

    override fun getShoppingListItems(f: ShoppingListItemsFilter, offset: Long, count: Int): Single<List<ShoppingListItem>> {
        return auth.getJWT()
                .flatMap { client.getShoppingListItems(it.value, f, offset, count) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(mutableListOf<ShoppingListItem>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "get shopping list items"))
                }
    }

    override fun getShoppingLists(offset: Long, count: Int): Single<List<ShoppingList>> {
        return auth.getJWT()
                .flatMap { client.getShoppingLists(it.value, offset, count) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(mutableListOf<ShoppingList>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "fetch shopping lists"))
                }
    }

    override fun createShoppingList(name: String): Single<ShoppingList> {
        return auth.getJWT()
                .flatMap { client.addShoppingList(it.value, name) }
                .onErrorResumeNext { Single.error(handleAuthErrors(logger, auth, resMan, it, "add shopping list")) }
    }

    private fun validateShoppingListItemInsert(item: ShoppingListItemInsert) = Completable.create {
        if (item.shoppingListID == "") {
            it.onError(Exception("shoppingListID cannot be empty"))
            return@create
        }
        if (item.itemName == "") {
            it.onError(Exception("itemName cannot be empty"))
            return@create
        }
        it.onCompleted()
    }

    private fun validateShoppingListItemUpdate(item: ShoppingListItemUpdate) = Completable.create {
        if (item.shoppingListID == "") {
            it.onError(Exception("shoppingListID cannot be empty"))
            return@create
        }
        if (item.shoppingListItemID == "") {
            it.onError(Exception("shoppingListItemID name cannot be empty"))
            return@create
        }
        it.onCompleted()
    }
}