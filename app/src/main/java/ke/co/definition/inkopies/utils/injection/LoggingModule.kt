package ke.co.definition.inkopies.utils.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.utils.logging.CrashlyticsLogger
import ke.co.definition.inkopies.utils.logging.Logger
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 20/03/18.
 */
@Module
class LoggingModule {

    // DO NOT use @Singleton because each Logger instance holds
    // a unique tag for the respective class.
    @Provides
    @Inject
    fun provideLogger(app: Application): Logger = CrashlyticsLogger(app)
}