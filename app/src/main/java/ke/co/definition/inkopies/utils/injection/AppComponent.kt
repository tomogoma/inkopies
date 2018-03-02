package ke.co.definition.inkopies.utils.injection

import dagger.Component
import ke.co.definition.inkopies.presentation.login.LoginViewModel
import ke.co.definition.inkopies.presentation.verification.VerificationViewModel
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Singleton
@Component(modules = [AppModule::class, Dagger2Module::class, AuthModule::class])
interface AppComponent {
    fun loginVMFactory(): LoginViewModel.Factory
    fun verificationVMFactory(): VerificationViewModel.Factory
}