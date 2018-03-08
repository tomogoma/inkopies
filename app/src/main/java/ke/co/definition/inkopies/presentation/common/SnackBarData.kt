package ke.co.definition.inkopies.presentation.common

import android.support.design.widget.Snackbar
import android.view.View
import ke.co.definition.inkopies.R

/**
 * Created by tomogoma
 * On 28/02/18.
 */
sealed class SnackBarData {
    abstract fun show(v: View)

    internal fun show(sb: Snackbar) {
        if (sb.duration == Snackbar.LENGTH_INDEFINITE) {
            sb.setAction(R.string.dismiss, { sb.dismiss() })
            sb.show()
        } else {
            sb.show()
        }
    }
}

class ResIDSnackBarData(
        private val resID: Int,
        private val duration: Int
) : SnackBarData() {
    override fun show(v: View) = show(Snackbar.make(v, resID, duration))
}

class TextSnackBarData(
        private val text: String,
        private val duration: Int
) : SnackBarData() {

    constructor(error: Throwable, duration: Int) : this(error.message ?: "", duration)

    override fun show(v: View) = show(Snackbar.make(v, text, duration))
}