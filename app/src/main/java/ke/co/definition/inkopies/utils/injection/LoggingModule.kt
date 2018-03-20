package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.utils.logging.LogcatLogger
import ke.co.definition.inkopies.utils.logging.Logger

/**
 * Created by tomogoma
 * On 20/03/18.
 */
@Module
class LoggingModule {

    // DO NOT use @Singleton because each Logger instance holds
    // a unique tag for the respective class.
    @Provides
    fun provideLogger(): Logger = LogcatLogger()
}