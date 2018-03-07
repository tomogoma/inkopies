package ke.co.definition.inkopies.utils.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.ResourceManagerImpl
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides
    @Singleton
    fun provideResourceManager(): ResourceManager = ResourceManagerImpl(app)
}