package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.shopping.CheckoutManager
import ke.co.definition.inkopies.model.shopping.CheckoutManagerImpl
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import ke.co.definition.inkopies.utils.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 29/05/18.
 */
@Module
class CheckoutModule {

    @Provides
    @Inject
    @Singleton
    fun provideCheckoutManager(a: Authable, sc: ShoppingClient, rm: ResourceManager, lg: Logger):
            CheckoutManager = CheckoutManagerImpl(a, sc, rm, lg)
}