package ke.co.definition.inkopies

import android.app.Application
import ke.co.definition.inkopies.model.Model

/**
 * Created by tomogoma on 08/07/17.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Model.init(this)
    }
}
