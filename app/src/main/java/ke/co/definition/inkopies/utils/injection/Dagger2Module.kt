package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Named

/**
 * Created by tomogoma
 * On 28/02/18.
 */
@Module
class Dagger2Module {

    companion object {
        const val SCHEDULER_IO = "SCHEDULER_IO"
        const val SCHEDULER_MAIN_THREAD = "SCHEDULER_MAIN_THREAD"
    }

    @Provides
    @Named(SCHEDULER_IO)
    fun provideIOScheduler(): Scheduler = Schedulers.io()

    @Provides
    @Named(SCHEDULER_MAIN_THREAD)
    fun provideMainThreadScheduler(): Scheduler = AndroidSchedulers.mainThread()

}