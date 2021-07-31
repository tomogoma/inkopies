package ke.co.definition.inkopies.repos.room

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.model.shopping.Brand
import ke.co.definition.inkopies.model.shopping.Category
import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.model.stores.Store
import ke.co.definition.inkopies.model.stores.StoreBranch
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import java.util.*

class RoomShoppingClient(private val shoppingListDao: ShoppingListDao,
                         private val measurementDao: MeasurementDao,
                         private val categoryDao: CategoryDao,
                         private val brandDao: BrandDao,
                         private val storeDao: StoreDao,
                         private val shoppingListItemNameDao: ShoppingListItemNameDao,
                         private val storeBranchDao: StoreBranchDao,
                         private val priceDao: PriceDao,
                         private val checkoutDao: CheckoutDao,
                         private val shoppingListItemDao: ShoppingListItemDao,
                         private val checkoutItemDao: CheckoutItemDao) : ShoppingClient {

    override fun addShoppingList(token: String, name: String): Single<ShoppingList> {
        val sl = ShoppingList(name, ShoppingMode.PREPARATION.name)
        return shoppingListDao.insert(sl)
                .map { toModelEntity(ShoppingList(sl.name, sl.mode, it.toInt())) }
    }

    override fun updateShoppingList(token: String, list: ShoppingList): Single<ShoppingList> {
        val sl = fromModelEntity(list)
        return shoppingListDao.update(sl)
                .map {
                    if (it != 1) {
                        throw Exception("update not successful")
                    }
                    toModelEntity(sl)
                }
    }

    override fun getShoppingLists(token: String, offset: Long, count: Int): Observable<List<ShoppingList>> {
        return shoppingListDao.get(offset, count)
                .toObservable()
                .map { it.map { sl -> toModelEntity(sl) } }
    }

    override fun getShoppingList(token: String, id: String): Observable<ShoppingList> {
        return shoppingListDao.getById(id.toInt())
                .toObservable()
                .map { toModelEntity(it) }
    }

    override fun insertShoppingListItem(token: String, req: ShoppingListItemInsert): Single<ShoppingListItem> {
        var measuringUnit: MeasuringUnit? = null
        var category: Category? = null
        var brand: Brand? = null
        var itemName: ShoppingItem? = null
        var brandPrice: BrandPrice? = null
        return insertIfPresent(req.measuringUnit, { measurementDao.insert(Measurement(it)) })
                .map {
                    measuringUnit = MeasuringUnit(it.toString(), req.measuringUnit ?: "")
                }

                .flatMap { insertIfPresent(req.categoryName, { categoryDao.insert(Category(it)) }) }
                .map { category = Category(it.toString(), req.categoryName ?: "") }

                .flatMap { shoppingListItemNameDao.insert(ShoppingListItemName(req.itemName)) }
                .map { itemName = ShoppingItem(it.toString(), req.itemName) }

                .flatMap { insertIfPresent(req.brandName, { brandDao.insert(Brand(it)) }) }
                .map {
                    brand = Brand(it.toString(), req.brandName ?: "",
                            measuringUnit!!, itemName!!)
                }

                .flatMap {
                    insertIfPresent(req.unitPrice, {
                        priceDao.insert(Price(itemName!!.id.toLong(), it, brand?.id?.toLong(),
                                measuringUnit!!.id.toLong()))
                    })
                }
                .map {
                    brandPrice = BrandPrice(it.toString(), req.unitPrice ?: 0F, brand!!,
                            StoreBranch("", "", Store("", "")))
                }

                .flatMap {
                    shoppingListItemDao.insert(ShoppingListItem(req.shoppingListID.toLong(),
                            itemName!!.id.toLong(), req.inList, req.inCart, req.quantity,
                            category?.id?.toLong(), brand?.id?.toLong(), measuringUnit?.id?.toLong()))
                }
                .map {
                    ShoppingListItem(it.toString(), req.quantity ?: 0, brandPrice!!,
                            category!!, req.inList, req.inCart)
                }
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

    private fun <T> insertIfPresent(value: T?, f: (T) -> Single<Long>): Single<Long?> {
        if (value == null) {
            return Single.just(null)
        }
        return f(value).map { val ret: Long? = it; ret }
    }

    private fun toModelEntity(sl: ke.co.definition.inkopies.repos.room.ShoppingList): ShoppingList {
        return ShoppingList(sl.id.toString(), sl.name, 0f, 0f, ShoppingMode.valueOf(sl.mode))
    }

    private fun toModelEntity(m: Measurement): MeasuringUnit {
        return MeasuringUnit(m.id.toString(), m.name)
    }

    private fun toModelEntity(m: ke.co.definition.inkopies.repos.room.Category): Category {
        return Category(m.id.toString(), m.name)
    }

    private fun toModelEntity(m: ke.co.definition.inkopies.repos.room.Store): Store {
        return Store(m.id.toString(), m.name)
    }

    private fun toModelEntity(m: ke.co.definition.inkopies.repos.room.StoreBranch, st: Store): StoreBranch {
        return StoreBranch(m.id.toString(), m.name, st)
    }

    private fun toModelEntity(m: ke.co.definition.inkopies.repos.room.Brand, mu: MeasuringUnit, si: ShoppingItem): Brand {
        return Brand(m.id.toString(), m.name, mu, si)
    }

    private fun toModelEntity(m: ke.co.definition.inkopies.repos.room.Price, br: Brand, sb: StoreBranch): BrandPrice {
        return BrandPrice(m.id.toString(), m.unitPrice, br, sb)
    }

    private fun fromModelEntity(sl: ShoppingList): ke.co.definition.inkopies.repos.room.ShoppingList {
        return ShoppingList(sl.name, sl.mode.name, sl.id.toInt())
    }
}