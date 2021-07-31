package ke.co.definition.inkopies.repos.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
        version = 1,
        entities = [
            ShoppingList::class,
            Category::class,
            ShoppingListItemName::class,
            Brand::class,
            Store::class,
            StoreBranch::class,
            Measurement::class,
            Price::class,
            Checkout::class,
            CheckoutItem::class,
            ShoppingListItem::class
        ]
)
abstract class ShoppingListDB : RoomDatabase() {
    abstract fun ShoppingListDao(): ShoppingListDao
    abstract fun MeasurementDao(): MeasurementDao
    abstract fun CategoryDao(): CategoryDao
    abstract fun BrandDao(): BrandDao
    abstract fun StoreDao(): StoreDao
    abstract fun ShoppingListItemNameDao(): ShoppingListItemNameDao
    abstract fun StoreBranchDao(): StoreBranchDao
    abstract fun PriceDao(): PriceDao
    abstract fun CheckoutDao(): CheckoutDao
    abstract fun ShoppingListItemDao(): ShoppingListItemDao
    abstract fun CheckoutItemDao(): CheckoutItemDao
}