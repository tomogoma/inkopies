package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.model.shopping.ShoppingManagerImpl
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import ke.co.definition.inkopies.repos.room.RoomShoppingClient
import ke.co.definition.inkopies.repos.room.ShoppingListDB
import ke.co.definition.inkopies.utils.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 22/03/18.
 */
@Module
class ShoppingModule {

    @Provides
    @Inject
    fun provideShoppingClient(db: ShoppingListDB):
            ShoppingClient = RoomShoppingClient(
            db.ShoppingListDao(),
            db.MeasurementDao(),
            db.CategoryDao(),
            db.BrandDao(),
            db.StoreDao(),
            db.ShoppingListItemNameDao(),
            db.StoreBranchDao(),
            db.PriceDao(),
            db.CheckoutDao(),
            db.ShoppingListItemDao(),
            db.CheckoutItemDao()
    )

    @Provides
    @Inject
    @Singleton
    fun provideShopingManager(cl: ShoppingClient, auth: Authable, lg: Logger, rm: ResourceManager):
            ShoppingManager = ShoppingManagerImpl(cl, auth, lg, rm)
}