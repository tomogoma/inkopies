package ke.co.definition.inkopies.repos.ms.shopping

import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.model.shopping.ShoppingListItemRequest
import ke.co.definition.inkopies.model.shopping.ShoppingListItemSearch
import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 23/03/18.
 */
interface ShoppingClient {
    fun addShoppingList(token: String, name: String): Single<ShoppingList>
    fun getShoppingLists(token: String, offset: Long, count: Int): Single<List<ShoppingList>>
    fun updateShoppingListItem(token: String, item: ShoppingListItem): Single<ShoppingListItem>
    fun getShoppingListItems(token: String, shoppingListID: String, offset: Long, count: Int): Single<List<ShoppingListItem>>
    fun upsertShoppingListItem(token: String, req: ShoppingListItemRequest): Single<ShoppingListItem>
    fun deleteShoppingListItem(token: String, id: String): Completable
    fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>>
}