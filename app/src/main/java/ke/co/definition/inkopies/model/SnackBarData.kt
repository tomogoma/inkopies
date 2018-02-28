package ke.co.definition.inkopies.model

import android.support.design.widget.Snackbar
import android.view.View

/**
 * Created by tomogoma
 * On 28/02/18.
 */
class SnackBarData(
        private val resID: Int,
        private val duration: Int
) {
    fun show(v: View) {
        Snackbar.make(v, resID, duration).show()
    }
}