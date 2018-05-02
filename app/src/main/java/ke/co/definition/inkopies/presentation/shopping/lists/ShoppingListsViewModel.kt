package ke.co.definition.inkopies.presentation.shopping.lists

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import android.net.Uri
import android.support.annotation.UiThread
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.backup.Exporter
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.presentation.common.ResIDSnackbarData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 20/03/18.
 */
class ShoppingListsViewModel @Inject constructor(
        private val manager: ShoppingManager,
        private val exporter: Exporter,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val progressShoppingLists = ObservableField<Boolean>()
    val showNoShoppingListsText = ObservableField<Boolean>()
    val showShoppingLists = ObservableField<Boolean>()

    val progressNextPage = SingleLiveEvent<Boolean>()
    val snackbarData = SingleLiveEvent<SnackbarData>()
    val shoppingLists = MutableLiveData<MutableList<VMShoppingList>>()

    fun start() {
        manager.getShoppingLists(0, LISTS_PER_PAGE)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .doOnSubscribe { showProgressShoppingLists() }
                .doOnUnsubscribe { hideProgressShoppingLists() }
                .map {
                    val res = mutableListOf<VMShoppingList>()
                    it.forEach { res.add(VMShoppingList(it)) }
                    return@map res
                }
                .subscribe(this::onShoppingListsFetched, this::showError)
    }

    fun onExport() {
        exporter.exportShoppingLists()
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onExportSuccessful, this::showError)
    }

    fun onImport(uri: Uri) {
        exporter.importLists(uri)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onImportSuccessful, this::showError)
    }

    private fun onExportSuccessful() {
        snackbarData.value = ResIDSnackbarData(R.string.export_successful,
                Snackbar.LENGTH_LONG)
    }

    private fun onImportSuccessful() {
        snackbarData.value = ResIDSnackbarData(R.string.import_successful,
                Snackbar.LENGTH_LONG)
    }

    private fun showError(err: Throwable) {
        snackbarData.value = TextSnackbarData(err, Snackbar.LENGTH_LONG)
    }

    private fun onShoppingListsFetched(sls: MutableList<VMShoppingList>) {
        shoppingLists.value = sls
        if (sls.size == 0)
            showNoShoppingListsText()
        else
            showShoppingLists()
    }

    private fun showShoppingLists() {
        progressShoppingLists.set(false)
        showNoShoppingListsText.set(false)
        showShoppingLists.set(true)
    }

    private fun showProgressShoppingLists() {
        showNoShoppingListsText.set(false)
        progressShoppingLists.set(true)
    }

    @UiThread
    private fun hideProgressShoppingLists() {
        progressShoppingLists.set(false)
        progressNextPage.value = false
    }

    private fun showNoShoppingListsText() {
        showShoppingLists.set(false)
        progressShoppingLists.set(false)
        showNoShoppingListsText.set(true)
    }

    companion object {
        private const val LISTS_PER_PAGE = 100
    }

    class Factory @Inject constructor(
            private val manager: ShoppingManager,
            private val exporter: Exporter,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListsViewModel(manager, exporter, subscribeOnScheduler, observeOnScheduler)
                    as T
        }
    }
}