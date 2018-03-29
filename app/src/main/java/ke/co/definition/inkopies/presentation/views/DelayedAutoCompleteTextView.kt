package ke.co.definition.inkopies.presentation.views

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import java.lang.ref.WeakReference

/**
 * Created by tomogoma
 * On 29/03/18.
 */
class DelayedAutoCompleteTextView(context: Context?, attrs: AttributeSet?) : AutoCompleteTextView(context, attrs) {

    private var delayMillis: Long = DEFAULT_AUTOCOMPLETE_DELAY_MILLIS
    private val handler = DelayHandler(WeakReference(this))

    override fun performFiltering(text: CharSequence, keyCode: Int) {
        handler.removeMessages(HANDLER_MESSAGE_ID)
        val msg = handler.obtainMessage(HANDLER_MESSAGE_ID, keyCode, 0, text)
        handler.sendMessageDelayed(msg, delayMillis)
        // Intentionally avoid calling super.performFiltering(text, keyCode)
        // until handler has handled delay and calls readyPerformFiltering(text, keyCode)
    }

    internal fun readyPerformFiltering(text: CharSequence, keyCode: Int) {
        super.performFiltering(text, keyCode)
    }

    companion object {
        private const val HANDLER_MESSAGE_ID = 0
        private const val DEFAULT_AUTOCOMPLETE_DELAY_MILLIS = 750L
    }

    class DelayHandler(
            private val delayedAutoCompleteTVWR: WeakReference<DelayedAutoCompleteTextView>
    ) : Handler() {

        override fun handleMessage(msg: Message) {
            // obj and arg1 of msg are parcelled in performFiltering during obtainMessage.
            delayedAutoCompleteTVWR.get()?.readyPerformFiltering(msg.obj as CharSequence, msg.arg1)
        }
    }

}