package ke.co.definition.inkopies.presentation.shopping.lists

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Scheduler
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class NewShoppingListViewModel @Inject constructor(
        private val man: ShoppingManager,
        private val resMan: ResourceManager,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val name = ObservableField<String>()
    val nameError = ObservableField<String>()
    val progress = ObservableField<ProgressData>()

    val finished = SingleLiveEvent<VMShoppingList>()
    val snackbarData = SingleLiveEvent<SnackbarData>()

    init {
        name.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val err = nameError.get() ?: return
                if (err == "") {
                    return
                }
                nameError.set("")
            }
        })
    }

    fun onCreateShoppingList() {
        val nameStr = name.get()
        if (nameStr == null || nameStr == "") {
            nameError.set(resMan.getString(R.string.error_required_field))
            return
        }
        man.createShoppingList(nameStr)
                .doOnSubscribe { progress.set(ProgressData(resMan.getString(R.string.creating_shopping_list))) }
                .doOnTerminate { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingList(it) }
                .subscribe({ finished.value = it }, {
                    snackbarData.value = TextSnackbarData(it, Snackbar.LENGTH_LONG)
                })
    }

    class Factory @Inject constructor(
            private val man: ShoppingManager,
            private val resMan: ResourceManager,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NewShoppingListViewModel(man, resMan, subscribeOnScheduler, observeOnScheduler)
                    as T
        }

    }
}