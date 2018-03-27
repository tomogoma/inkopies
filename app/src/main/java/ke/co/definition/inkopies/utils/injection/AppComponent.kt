package ke.co.definition.inkopies.utils.injection

import dagger.Component
import ke.co.definition.inkopies.presentation.login.LoginViewModel
import ke.co.definition.inkopies.presentation.profile.GeneralProfileViewModel
import ke.co.definition.inkopies.presentation.profile.ProfileViewModel
import ke.co.definition.inkopies.presentation.shopping.list.ShoppingListViewModel
import ke.co.definition.inkopies.presentation.shopping.list.UpsertListItemViewModel
import ke.co.definition.inkopies.presentation.shopping.lists.NewShoppingListViewModel
import ke.co.definition.inkopies.presentation.shopping.lists.ShoppingListsViewModel
import ke.co.definition.inkopies.presentation.verification.UpdateIdentifierViewModel
import ke.co.definition.inkopies.presentation.verification.VerificationViewModel
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Singleton
@Component(modules = [
    AppModule::class,
    Dagger2Module::class,
    AuthModule::class,
    UserModule::class,
    ImageModule::class,
    LoggingModule::class,
    ShoppingModule::class
])
interface AppComponent {
    fun loginVMFactory(): LoginViewModel.Factory
    fun verificationVMFactory(): VerificationViewModel.Factory
    fun updateIdentifierVMFactory(): UpdateIdentifierViewModel.Factory
    fun generalProfileVMFactory(): GeneralProfileViewModel.Factory
    fun profileVMFactory(): ProfileViewModel.Factory
    fun provideNewShoppingListVMFactory(): NewShoppingListViewModel.Factory
    fun provideShoppingListsVMFactory(): ShoppingListsViewModel.Factory
    fun provideShoppingListVMFactory(): ShoppingListViewModel.Factory
    fun provideUpsertListItemVMFactory(): UpsertListItemViewModel.Factory
}