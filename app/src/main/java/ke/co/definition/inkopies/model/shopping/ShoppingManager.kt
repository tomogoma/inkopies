package ke.co.definition.inkopies.model.shopping

import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 22/03/18.
 */
interface ShoppingManager {

    fun createShoppingList(name: String): Single<ShoppingList>
    fun updateShoppingList(list: ShoppingList): Single<ShoppingList>
    fun getShoppingLists(offset: Long, count: Int): Single<List<ShoppingList>>

    fun insertShoppingListItem(item: ShoppingListItemInsert): Single<ShoppingListItem>
    fun updateShoppingListItem(req: ShoppingListItemUpdate): Single<ShoppingListItem>
    fun deleteShoppingListItem(shoppingListID: String, id: String): Completable
    fun getShoppingListItems(shoppingListID: String, offset: Long, count: Int): Single<List<ShoppingListItem>>
    fun searchShoppingListItem(req: ShoppingListItemSearch): Single<List<ShoppingListItem>>
}