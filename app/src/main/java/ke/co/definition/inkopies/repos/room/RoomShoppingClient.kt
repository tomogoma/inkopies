package ke.co.definition.inkopies.repos.room

import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.model.shopping.Category
import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import rx.Completable
import rx.Observable
import rx.Single
import java.util.*

class RoomShoppingClient(private val shoppingListDao: ShoppingListDao) : ShoppingClient {

    override fun addShoppingList(token: String, name: String): Single<ShoppingList> {
        val sl = ShoppingList(0, name, ShoppingMode.PREPARATION.name)
        return shoppingListDao.insert(sl)
                .map {
                    return@map toModelEntity(ShoppingList(it, sl.name, sl.mode))
                }
    }

    private fun toModelEntity(sl: ke.co.definition.inkopies.repos.room.ShoppingList): ShoppingList {
        return ShoppingList(sl.id.toString(), sl.name, 0f, 0f, ShoppingMode.valueOf(sl.mode))
    }

    private fun fromModelEntity(sl: ShoppingList): ke.co.definition.inkopies.repos.room.ShoppingList {
        return ShoppingList(sl.id.toInt(), sl.name, sl.mode.name)
    }

    override fun updateShoppingList(token: String, list: ShoppingList): Single<ShoppingList> {
        val sl = fromModelEntity(list)
        return shoppingListDao.update(sl)
                .map { toModelEntity(sl) }
    }

    override fun getShoppingLists(token: String, offset: Long, count: Int): Observable<List<ShoppingList>> {
        return shoppingListDao.get(offset, count)
                .map { it.map { sl -> toModelEntity(sl) } }
    }

    override fun getShoppingList(token: String, id: String): Observable<ShoppingList> {
        return shoppingListDao.getById(id.toInt())
                .map { toModelEntity(it) }
    }

    override fun insertShoppingListItem(token: String, req: ShoppingListItemInsert): Single<ShoppingListItem> {
        TODO("Not yet implemented")
    }

    override fun updateShoppingListItem(token: String, update: ShoppingListItemUpdate): Single<ShoppingListItem> {
        TODO("Not yet implemented")
    }

    override fun deleteShoppingListItem(token: String, shoppingListID: String, id: String): Completable {
        TODO("Not yet implemented")
    }

    override fun getShoppingListItems(token: String, f: ShoppingListItemsFilter): Observable<List<ShoppingListItem>> {
        TODO("Not yet implemented")
    }

    override fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>> {
        TODO("Not yet implemented")
    }

    override fun searchCategory(token: String, q: String): Single<List<Category>> {
        TODO("Not yet implemented")
    }

    override fun checkout(token: String, slid: String, branchName: String?, storeName: String?, date: Date): Completable {
        TODO("Not yet implemented")
    }
}