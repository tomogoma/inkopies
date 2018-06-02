package ke.co.definition.inkopies.model.shopping

import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import ke.co.definition.inkopies.utils.logging.Logger
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Observable
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
                .map { throw list.validate() ?: return@map it }
                .flatMap { client.updateShoppingList(it.value, list) }
                .onErrorResumeNext { Single.error(handleAuthErrors(logger, auth, resMan, it, "update shopping list")) }
    }

    override fun searchCategory(q: String): Single<List<Category>> {
        return auth.getJWT()
                .flatMap { client.searchCategory(it.value, q) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(listOf<Category>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "search category"))
                }
    }

    override fun searchShoppingListItem(req: ShoppingListItemSearch): Single<List<ShoppingListItem>> {
        return auth.getJWT()
                .flatMap { client.searchShoppingListItem(it.value, req) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(listOf<ShoppingListItem>())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "search shopping list item"))
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
        return Completable
                .create {
                    auth.getJWT()
                            .subscribe({ jwt ->
                                client.deleteShoppingListItem(jwt.value, shoppingListID, id)
                                        .subscribe(it::onCompleted, it::onError)
                            }, it::onError)
                }
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

    override fun getShoppingListItems(f: ShoppingListItemsFilter): Observable<List<ShoppingListItem>> {
        return auth.getJWT()
                .toObservable()
                .flatMap { client.getShoppingListItems(it.value, f) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Observable.just(mutableListOf<ShoppingListItem>())
                    }
                    return@onErrorResumeNext Observable.error(handleAuthErrors(logger, auth,
                            resMan, it, "get shopping list items"))
                }
    }

    override fun getShoppingLists(offset: Long, count: Int): Observable<List<ShoppingList>> {
        return auth.getJWT()
                .toObservable()
                .flatMap { client.getShoppingLists(it.value, offset, count) }
                .onErrorReturn {
                    throw handleAuthErrors(logger, auth, resMan, it, "fetch shopping lists")
                }
    }

    override fun getShoppingList(id: String): Observable<ShoppingList> {
        return auth.getJWT()
                .toObservable()
                .flatMap { client.getShoppingList(it.value, id) }
                .onErrorReturn {
                    throw handleAuthErrors(logger, auth, resMan, it, "fetch shopping list")
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
        if (item.inCart && !item.inList) {
            it.onError(Exception("cannot be inCart but not inList"))
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
        // Only one of inCart or inList provided. Need either both or none provided.
        if ((item.inCart != null).xor(item.inList != null)) {
            it.onError(Exception("inCart and inList must be provided together"))
            return@create
        }
        // xor operator above guarantees inList will not be null if inCart is not null.
        if (item.inCart != null && item.inCart && !item.inList!!) {
            it.onError(Exception("cannot be inCart but not inList"))
            return@create
        }
        it.onCompleted()
    }
}