package ke.co.definition.inkopies.repos.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
        version = 1,
        entities = [
            ShoppingList::class
        ]
)
abstract class ShoppingListDB : RoomDatabase() {
    abstract fun ShoppingListDao(): ShoppingListDao
}