package ke.co.definition.inkopies.model.shopping

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by tomogoma
 * On 22/03/18.
 */
interface ShoppingManager {

    fun createShoppingList(name: String): Single<ShoppingList>
    fun updateShoppingList(list: ShoppingList): Single<ShoppingList>
    fun getShoppingLists(offset: Long, count: Int): Observable<List<ShoppingList>>
    fun getShoppingList(id: String): Observable<ShoppingList>

    fun insertShoppingListItem(item: ShoppingListItemInsert): Single<ShoppingListItem>
    fun updateShoppingListItem(req: ShoppingListItemUpdate): Single<ShoppingListItem>
    fun deleteShoppingListItem(shoppingListID: String, id: String): Completable
    fun getShoppingListItems(f: ShoppingListItemsFilter): Observable<Pair<DiffUtil.DiffResult, List<ShoppingListItem>>>
    fun searchShoppingListItem(req: ShoppingListItemSearch): Single<List<ShoppingListItem>>
    fun searchCategory(q: String): Single<List<Category>>
}