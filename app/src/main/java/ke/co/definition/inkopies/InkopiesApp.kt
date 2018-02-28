package ke.co.definition.inkopies

import android.app.Application
import ke.co.definition.inkopies.utils.injection.AppComponent
import ke.co.definition.inkopies.utils.injection.AppModule
import ke.co.definition.inkopies.utils.injection.DaggerAppComponent

/**
 * Created by tomogoma
 * On 28/02/18.
 */
class InkopiesApp : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = initDagger(this)
    }

    private fun initDagger(app: InkopiesApp): AppComponent =
            DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()
}