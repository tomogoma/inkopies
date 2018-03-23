package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.model.shopping.ShoppingManagerImpl
import ke.co.definition.inkopies.repos.ms.shopping.MockShoppingClient
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import ke.co.definition.inkopies.utils.logging.Logger
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 22/03/18.
 */
@Module
class ShoppingModule {

    @Provides
    fun provideShoppingClient(): ShoppingClient = MockShoppingClient()

    @Provides
    @Inject
    fun provideShopingManager(cl: ShoppingClient, auth: Authable, lg: Logger, rm: ResourceManager): ShoppingManager {
        return ShoppingManagerImpl(cl, auth, lg, rm)
    }
}