package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.model.FileHelper
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.backup.Exporter
import ke.co.definition.inkopies.model.backup.ExporterImpl
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.utils.logging.Logger
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 10/04/18.
 */
@Module
class ExportModule {

    @Provides
    @Inject
    fun provideExporter(sm: ShoppingManager, fh: FileHelper, lg: Logger, auth: Authable, rm: ResourceManager)
            : Exporter = ExporterImpl(sm, fh, lg, auth, rm)
}