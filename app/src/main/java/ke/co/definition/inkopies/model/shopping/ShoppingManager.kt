package ke.co.definition.inkopies.model.shopping

import rx.Single

/**
 * Created by tomogoma
 * On 22/03/18.
 */
interface ShoppingManager {
    fun createShoppingList(name: String): Single<ShoppingList>
    fun getShoppingLists(offset: Long, count: Int): Single<List<ShoppingList>>
    fun getShoppingListItems(shoppingListID: String, offset: Long, count: Int): Single<List<ShoppingListItem>>
}