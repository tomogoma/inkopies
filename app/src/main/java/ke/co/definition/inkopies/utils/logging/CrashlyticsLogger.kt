package ke.co.definition.inkopies.utils.logging

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.BuildConfig
import javax.inject.Inject


/**
 * Created by tomogoma
 * On 07/04/18.
 */
class CrashlyticsLogger @Inject constructor(app: Application) : Logger {

    private var tag = App::class.java.name

    init {
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(app, crashlyticsKit)
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