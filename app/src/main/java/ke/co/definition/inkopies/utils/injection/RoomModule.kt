package ke.co.definition.inkopies.utils.injection

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.repos.room.ShoppingListDB
import javax.inject.Inject

@Module
class RoomModule {

    @Provides
    @Inject
    fun provideRoomDatabase(application: Application): ShoppingListDB =
            Room.databaseBuilder(application, ShoppingListDB::class.java, "shopping")
                    .fallbackToDestructiveMigration()
                    .build()
}