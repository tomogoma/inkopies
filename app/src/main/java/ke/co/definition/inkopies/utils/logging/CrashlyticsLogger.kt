package ke.co.definition.inkopies.utils.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.BuildConfig
import javax.inject.Inject


/**
 * Created by tomogoma
 * On 07/04/18.
 */
class CrashlyticsLogger @Inject constructor() : Logger {

    private var tag = App::class.java.name

    init {
        val disableCollectionForDebugBuilds = !BuildConfig.DEBUG
        FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(disableCollectionForDebugBuilds)
    }

    override fun setTag(tag: String) {
        this.tag = tag
    }

    override fun warn(msg: String) {
        FirebaseCrashlytics.getInstance().log("W/$tag: $msg")
    }

    override fun error(msg: String, e: Throwable?) {
        val noDetails = "<no details>"
        val cltks = FirebaseCrashlytics.getInstance()
        cltks.log("E/$tag: $msg: ${e?.message ?: noDetails}")
        if (e != null) {
            cltks.recordException(e)
        }
    }

}