package ke.co.definition.inkopies.presentation.common

import android.arch.lifecycle.LiveData
import android.support.v4.app.Fragment

/**
 * Created by tomogoma
 * On 31/03/18.
 */
abstract class InkopiesFragment : Fragment() {

    internal val observedLiveData = mutableListOf<LiveData<*>>()

    override fun onDestroy() {
        removeLiveDataObservers()
        super.onDestroy()
    }

    internal fun removeLiveDataObservers() {
        observedLiveData.forEach { it.removeObservers(this) }
    }
}