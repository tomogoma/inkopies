package ke.co.definition.inkopies.repos.room

import androidx.room.Dao
import androidx.room.Query
import rx.Observable

@Dao
interface ShoppingListDao : CrudDao<ShoppingList> {

    @Query("SELECT * FROM shopping_lists LIMIT :count OFFSET :offset")
    fun get(offset: Long, count: Int): Observable<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE rowid = :id")
    fun getById(id: Int): Observable<ShoppingList>
}

@Dao
interface CategoryDao : CrudDao<Category> {
}

@Dao
interface ShoppingListItemNameDao : CrudDao<ShoppingListItemName> {
}

@Dao
interface BrandDao : CrudDao<Brand> {
}

@Dao
interface StoreDao : CrudDao<Store> {
}

@Dao
interface StoreBranchDao : CrudDao<StoreBranch> {
}

@Dao
interface MeasurementDao : CrudDao<Measurement> {
}

@Dao
interface ItemBrandPriceDao : CrudDao<ItemBrandPrice> {
}

@Dao
interface CheckoutDao : CrudDao<Checkout> {
}

@Dao
interface ShoppingListItemDao : CrudDao<ShoppingListItem> {
}

@Dao
interface CheckoutItemDao : CrudDao<CheckoutItem> {
}