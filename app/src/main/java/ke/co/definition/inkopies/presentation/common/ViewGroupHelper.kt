package ke.co.definition.inkopies.presentation.common

import android.view.ViewGroup
import android.view.ViewTreeObserver

/**
 * Created by tomogoma
 * On 28/03/18.
 */

/**
 * Register a ViewGroup#addOnGlobalLayoutListener which only listens for the first event
 * and then removes itself.
 */
fun ViewGroup.onGlobalLayoutOnce(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}