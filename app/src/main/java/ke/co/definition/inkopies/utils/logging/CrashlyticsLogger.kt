package ke.co.definition.inkopies.utils.logging

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import ke.co.definition.inkopies.App
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 07/04/18.
 */
class CrashlyticsLogger @Inject constructor(app: Application) : Logger {

    private var tag = App::class.java.name

    init {
        Fabric.with(app, Crashlytics())
    }

    override fun setTag(tag: String) {
        this.tag = tag
    }

    override fun warn(msg: String) {
        Crashlytics.log(Log.WARN, tag, msg)
    }

    override fun error(msg: String, e: Throwable?) {
        val noDetails = "<no details>"
        Crashlytics.log(Log.ERROR, tag, "$msg: ${e?.message ?: noDetails}")
        Crashlytics.logException(e)
    }

}