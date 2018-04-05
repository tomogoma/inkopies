package ke.co.definition.inkopies.presentation.common

import android.view.View
import android.widget.EditText

/**
 * Created by tomogoma
 * On 05/04/18.
 */
fun EditText.selectAllOnFocus() {
    onFocusChangeListener = View.OnFocusChangeListener { v, focused ->
        if (focused) (v as EditText).selectAll()
    }
}