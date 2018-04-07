package ke.co.definition.inkopies

import android.app.Application
import ke.co.definition.inkopies.utils.injection.AppComponent
import ke.co.definition.inkopies.utils.injection.AppModule
import ke.co.definition.inkopies.utils.injection.DaggerAppComponent

/**
 * Created by tomogoma
 * On 28/02/18.
 */
class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = initDagger(this)
        appComponent.instantiateLogging()// In case logger monitors crashes.
    }

    private fun initDagger(app: App): AppComponent =
            DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()
}