package ke.co.definition.inkopies.repos.room

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface ShoppingListDao : CrudDao<ShoppingList> {

    @Query("SELECT * FROM shopping_lists LIMIT :count OFFSET :offset")
    fun get(offset: Long, count: Int): Flowable<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE rowid = :id")
    fun getById(id: Int): Flowable<ShoppingList>
}

@Dao
interface CategoryDao : CrudWithIgnoreConflictDao<Category>

@Dao
interface ShoppingListItemNameDao : CrudWithIgnoreConflictDao<ShoppingListItemName>

@Dao
interface BrandDao : CrudWithIgnoreConflictDao<Brand>

@Dao
interface StoreDao : CrudDao<Store>

@Dao
interface StoreBranchDao : CrudDao<StoreBranch>

@Dao
interface MeasurementDao : CrudWithIgnoreConflictDao<Measurement>

@Dao
interface ItemBrandPriceDao : CrudWithReplaceOnConflictDao<ItemBrandPrice>

@Dao
interface CheckoutDao : CrudDao<Checkout>

@Dao
interface ShoppingListItemDao : CrudWithReplaceOnConflictDao<ShoppingListItem>

@Dao
interface CheckoutItemDao : CrudDao<CheckoutItem>