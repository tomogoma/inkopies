package ke.co.definition.inkopies.repos.ms.shopping

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ke.co.definition.inkopies.model.shopping.*
import java.util.*

/**
 * Created by tomogoma
 * On 23/03/18.
 */
interface ShoppingClient {

    fun addShoppingList(token: String, name: String): Single<ShoppingList>
    fun updateShoppingList(token: String, list: ShoppingList): Single<ShoppingList>
    fun getShoppingLists(token: String, offset: Long, count: Int): Observable<List<ShoppingList>>
    fun getShoppingList(token: String, id: String): Observable<ShoppingList>

    fun insertShoppingListItem(token: String, req: ShoppingListItemInsert): Single<ShoppingListItem>
    fun updateShoppingListItem(token: String, update: ShoppingListItemUpdate): Single<ShoppingListItem>
    fun deleteShoppingListItem(token: String, shoppingListID: String, id: String): Completable
    fun getShoppingListItems(token: String, f: ShoppingListItemsFilter): Observable<List<ShoppingListItem>>
    fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>>
    fun searchCategory(token: String, q: String): Single<List<Category>>
    fun checkout(token: String, slid: String, branchName: String?, storeName: String?, date: Date): Completable
}