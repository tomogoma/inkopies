package ke.co.definition.inkopies.utils.injection

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.JWTHelper
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.model.shopping.ShoppingManagerImpl
import ke.co.definition.inkopies.repos.firestore.FirebaseShoppingClient
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
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
    fun provideShoppingClient(jwtHelper: JWTHelper, lg: Logger, firestore: FirebaseFirestore):
            ShoppingClient = FirebaseShoppingClient(jwtHelper, lg, firestore)

    @Provides
    @Inject
    @Singleton
    fun provideShopingManager(cl: ShoppingClient, auth: Authable, lg: Logger, rm: ResourceManager):
            ShoppingManager = ShoppingManagerImpl(cl, auth, lg, rm)
}