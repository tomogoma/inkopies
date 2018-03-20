package ke.co.definition.inkopies.utils.logging

/**
 * Created by tomogoma
 * On 20/03/18.
 */
interface Logger {
    fun setTag(tag: String)
    fun warn(msg: String)
    fun error(msg: String, e: Throwable? = null)
}