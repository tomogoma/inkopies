package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.ProfileManagerImpl
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 09/03/18.
 */
@Module
class UserModule {

    @Provides
    @Inject
    fun provideProfileManager(auth: Authable): ProfileManager = ProfileManagerImpl(auth)
}