package ke.co.definition.inkopies.utils.injection

import android.app.Application
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.Authenticator
import ke.co.definition.inkopies.model.auth.Validatable
import ke.co.definition.inkopies.model.auth.Validator
import ke.co.definition.inkopies.repos.LocalStorable
import ke.co.definition.inkopies.repos.LocalStore
import ke.co.definition.inkopies.repos.ms.AUTH_MS_ADDRESS
import ke.co.definition.inkopies.repos.ms.AuthClient
import ke.co.definition.inkopies.repos.ms.AuthClientImpl
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Module
class AuthModule {

    @Provides
    @Named(NAME)
    fun provideRetrofit(): Retrofit {
        val gson = GsonBuilder()
                .setLenient()
                .create()
        return Retrofit.Builder()
                .baseUrl(AUTH_MS_ADDRESS)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    @Provides
    @Inject
    fun provideValidatable(): Validatable = Validator

    @Provides
    @Inject
    fun provideAuthClient(@Named(NAME) rf: Retrofit): AuthClient = AuthClientImpl(rf)

    @Provides
    @Inject
    fun provideLocalStorable(app: Application): LocalStorable = LocalStore(app)

    @Provides
    @Inject
    fun provideAuthable(ls: LocalStorable, authCl: AuthClient): Authable = Authenticator(ls, authCl)

    companion object {
        const val NAME = "AuthModule"
    }
}