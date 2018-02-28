package ke.co.definition.inkopies.utils.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.Authenticator
import ke.co.definition.inkopies.model.auth.Validatable
import ke.co.definition.inkopies.model.auth.Validator
import ke.co.definition.inkopies.repos.LocalStorable
import ke.co.definition.inkopies.repos.LocalStore
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Module
class AuthModule {

    @Provides
    @Inject
    fun provideAuthable(ls: LocalStorable): Authable = Authenticator(ls)

    @Provides
    @Inject
    fun provideLocalStorable(app: Application): LocalStorable = LocalStore(app)

    @Provides
    @Inject
    fun provideValidatable(): Validatable = Validator
}