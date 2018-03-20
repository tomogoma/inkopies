package ke.co.definition.inkopies.utils.logging

import android.util.Log
import ke.co.definition.inkopies.App

/**
 * Created by tomogoma
 * On 20/03/18.
 */
class LogcatLogger : Logger {

    private var tag: String = App::class.java.name

    override fun setTag(tag: String) {
        this.tag = tag
    }

    override fun warn(msg: String) {
        Log.w(tag, msg)
    }

    override fun error(msg: String, e: Throwable?) {
        Log.e(tag, msg, e)
    }

}