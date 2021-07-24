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
            ItemBrandPrice::class,
            Checkout::class,
            CheckoutItem::class,
            ShoppingListItem::class
        ]
)
abstract class ShoppingListDB : RoomDatabase() {
    abstract fun ShoppingListDao(): ShoppingListDao
}