package ke.co.definition.inkopies.presentation.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

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