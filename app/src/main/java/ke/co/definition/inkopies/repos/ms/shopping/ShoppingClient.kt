package ke.co.definition.inkopies.repos.ms.shopping

import ke.co.definition.inkopies.model.shopping.*
import rx.Completable
import rx.Observable
import rx.Single
import java.util.*

/**
 * Created by tomogoma
 * On 23/03/18.
 */
interface ShoppingClient {

    fun addShoppingList(token: String, name: String): Single<ShoppingList>
    fun updateShoppingList(token: String, list: ShoppingList): Single<ShoppingList>
    fun getShoppingLists(token: String, offset: Long, count: Int): Observable<List<ShoppingList>>

    fun insertShoppingListItem(token: String, req: ShoppingListItemInsert): Single<ShoppingListItem>
    fun updateShoppingListItem(token: String, update: ShoppingListItemUpdate): Single<ShoppingListItem>
    fun deleteShoppingListItem(token: String, shoppingListID: String, id: String): Completable
    fun getShoppingListItems(token: String, f: ShoppingListItemsFilter, offset: Long, count: Int): Single<List<ShoppingListItem>>
    fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>>
    fun searchCategory(token: String, q: String): Single<List<Category>>
    fun checkout(token: String, slid: String, branchName: String?, storeName: String?, date: Date): Completable
}