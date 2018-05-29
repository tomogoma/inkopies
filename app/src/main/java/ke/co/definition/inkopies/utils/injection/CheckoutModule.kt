package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.shopping.CheckoutManager
import ke.co.definition.inkopies.model.shopping.CheckoutManagerImpl
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
    fun provideCheckoutManager(rm: ResourceManager): CheckoutManager = CheckoutManagerImpl(rm)
}