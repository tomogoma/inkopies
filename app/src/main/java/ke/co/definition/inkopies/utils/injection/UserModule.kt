package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.ProfileManagerImpl
import ke.co.definition.inkopies.repos.ms.USERS_MS_ADDRESS
import ke.co.definition.inkopies.repos.ms.image.ImageClient
import ke.co.definition.inkopies.repos.ms.users.RetrofitUsersClient
import ke.co.definition.inkopies.repos.ms.users.UsersClient
import ke.co.definition.inkopies.utils.logging.Logger
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 09/03/18.
 */
@Module
class UserModule {

    @Provides
    @Named(MS)
    fun provideRetrofit(): Retrofit = retrofitFactory(USERS_MS_ADDRESS)

    @Provides
    @Inject
    fun provideUsersClient(@Named(MS) rf: Retrofit): UsersClient = RetrofitUsersClient(rf)

    @Provides
    @Inject
    fun provideProfileManager(rm: ResourceManager, a: Authable, ucl: UsersClient, imgCl: ImageClient, lg: Logger): ProfileManager {
        return ProfileManagerImpl(rm, a, ucl, imgCl, lg)
    }

    companion object {
        const val MS = "usersms"
    }
}