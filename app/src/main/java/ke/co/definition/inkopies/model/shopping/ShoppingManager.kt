package ke.co.definition.inkopies.model.shopping

import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 22/03/18.
 */
interface ShoppingManager {
    fun createShoppingList(name: String): Single<ShoppingList>
    fun getShoppingLists(offset: Long, count: Int): Single<List<ShoppingList>>
    fun getShoppingListItems(shoppingListID: String, offset: Long, count: Int): Single<List<ShoppingListItem>>
    fun updateShoppingListItem(item: ShoppingListItem): Single<ShoppingListItem>
    fun upsertShoppingListItem(req: ShoppingListItemUpsert): Single<ShoppingListItem>
    fun deleteShoppingListItem(id: String): Completable
    fun searchShoppingListItem(req: ShoppingListItemSearch): Single<List<ShoppingListItem>>
}