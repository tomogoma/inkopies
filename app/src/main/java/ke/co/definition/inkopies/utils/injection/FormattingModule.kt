package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.presentation.format.DateFormatter
import ke.co.definition.inkopies.presentation.format.DateFormatterImpl
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 29/05/18.
 */
@Module
class FormattingModule {

    @Provides
    @Singleton
    fun provideFormatter(): DateFormatter = DateFormatterImpl()
}