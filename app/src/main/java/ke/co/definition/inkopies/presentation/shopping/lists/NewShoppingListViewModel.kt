package ke.co.definition.inkopies.presentation.shopping.lists

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.Observable
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
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

    val finished = SingleLiveEvent<ShoppingList>()
    val snackbarData = SingleLiveEvent<SnackBarData>()

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
                .doOnUnsubscribe { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ finished.value = it }, {
                    snackbarData.value = TextSnackBarData(it, Snackbar.LENGTH_LONG)
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