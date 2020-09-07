package ke.co.definition.inkopies.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData

/**
 * Created by tomogoma
 * On 27/03/18.
 */
open class SLMDialogFragment : DialogFragment() {

    private var isDialog = false
    internal val observedLiveData: MutableList<LiveData<*>> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!isDialog) {
            throw RuntimeException("This DialogFragment only supports being started as a dialog" +
                    "because of how it finishes() which may cause NPE on non-dialogs")
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isDialog = true
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onDestroy() {
        observedLiveData.forEach { it.removeObservers(this) }
        super.onDestroy()
    }
}