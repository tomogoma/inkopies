package ke.co.definition.inkopies.presentation.common

import androidx.databinding.Observable
import androidx.databinding.ObservableField

/**
 * Created by tomogoma
 * On 28/03/18.
 */
fun ObservableField<*>.clearErrorOnChange(errorField: ObservableField<String>) {
    addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(p0: Observable?, p1: Int) {
            val err = errorField.get() ?: return
            if (err == "") {
                return
            }
            errorField.set("")
        }
    })
}