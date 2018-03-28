package ke.co.definition.inkopies.presentation.common

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

fun TextView.showKeyboard(context: Context) {
    this.postDelayed({
        requestFocus()
        val imm = getInputMethodMan(context)
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, 200)
}

fun TextView.hideKeyboard(context: Context) {
    this.postDelayed({
        val imm = getInputMethodMan(context)
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.SHOW_IMPLICIT)
    }, 200)
}

private fun getInputMethodMan(context: Context) =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager