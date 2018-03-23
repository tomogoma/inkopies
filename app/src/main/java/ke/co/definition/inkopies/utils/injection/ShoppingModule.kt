package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.model.shopping.ShoppingManagerImpl

/**
 * Created by tomogoma
 * On 22/03/18.
 */
@Module
class ShoppingModule {

    @Provides
    fun provideShopingManager(): ShoppingManager = ShoppingManagerImpl()
}