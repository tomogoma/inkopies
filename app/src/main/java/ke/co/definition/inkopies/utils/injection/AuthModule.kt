package ke.co.definition.inkopies.utils.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.Authenticator
import ke.co.definition.inkopies.model.auth.Validatable
import ke.co.definition.inkopies.model.auth.Validator
import ke.co.definition.inkopies.repos.local.LocalStorable
import ke.co.definition.inkopies.repos.local.LocalStore
import ke.co.definition.inkopies.repos.ms.AUTH_MS_ADDRESS
import ke.co.definition.inkopies.repos.ms.auth.AuthClient
import ke.co.definition.inkopies.repos.ms.auth.RetrofitAuthClient
import ke.co.definition.inkopies.utils.logging.Logger
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Module
class AuthModule {

    @Provides
    @Named(MS)
    fun provideRetrofit(): Retrofit = retrofitFactory(AUTH_MS_ADDRESS)

    @Provides
    @Inject
    fun provideAuthClient(@Named(MS) rf: Retrofit): AuthClient = RetrofitAuthClient(rf)

    @Provides
    @Inject
    @Singleton
    fun providePhoneNumberUtil(app: Application) = PhoneNumberUtil.createInstance(app)

    @Provides
    @Inject
    fun provideValidatable(pnu: PhoneNumberUtil): Validatable = Validator(pnu)

    @Provides
    @Inject
    fun provideLocalStorable(app: Application): LocalStorable = LocalStore(app)

    @Provides
    @Inject
    @Singleton
    fun provideAuthable(ls: LocalStorable, ac: AuthClient, v: Validatable, rm: ResourceManager, lg: Logger)
            : Authable = Authenticator(ls, ac, v, rm, lg)

    companion object {
        const val MS = "authms"
    }
}