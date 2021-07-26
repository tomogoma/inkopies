package ke.co.definition.inkopies.presentation.shopping.list

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.backup.Exporter
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.presentation.common.ResIDSnackbarData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class ShoppingListViewModel @Inject constructor(
        private val manager: ShoppingManager,
        private val exporter: Exporter,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val shoppingList = ObservableField<VMShoppingList>()
    val showItems = ObservableBoolean()
    val showNoItemsTxt = ObservableBoolean()
    val showFullProgress = ObservableBoolean()

    val snackbarData = SingleLiveEvent<SnackbarData>()
    val items = SingleLiveEvent<Pair<DiffUtil.DiffResult?, MutableList<VMShoppingListItem>>>()
    val menuRes = SingleLiveEvent<Int>()

    private var itemsSubscription: Disposable? = null
    private var subscriptions = CompositeDisposable()

    fun start(listID: String) {
        fetchList(listID)
    }

    fun onCreateOptionsMenu() {
        menuRes.value = when (shoppingList.get()?.mode ?: return) {
            ShoppingMode.PREPARATION -> R.menu.planning_main_menu
            ShoppingMode.SHOPPING -> R.menu.shopping_main_menu
        }
    }

    fun onChangeMode(toMode: ShoppingMode) {
        val list = shoppingList.get() ?: return
        subscriptions.add(manager.updateShoppingList(ShoppingList(list.id, list.name(), list.sl.activeListPrice,
                list.sl.cartPrice, toMode))
                .doOnSubscribe { showProgress() }
                .map { VMShoppingList(it) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onShoppingListUpdated, this::showError))
    }

    fun onItemSelectionChanged(item: VMShoppingListItem, toState: Boolean) {

        val list = shoppingList.get() ?: return

        var inList = item.inList
        var inCart = item.inCart
        when (item.mode) {
            ShoppingMode.SHOPPING -> {
                inCart = toState
                if (toState) {
                    inList = true
                }
            }
            ShoppingMode.PREPARATION -> {
                inList = toState
                if (!toState) {
                    inCart = false
                }
            }
        }

        subscriptions.add(manager.updateShoppingListItem(ShoppingListItemUpdate(list.id,
                item.id, categoryName = item.categoryName(), inList = inList, inCart = inCart))
                .doOnSubscribe { item.isUpdating.set(true) }
                .doAfterTerminate { item.isUpdating.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingListItem(it, item.mode) }
                .subscribe({ /*no-op - #fetchItems() is observing changes*/ }, { showError(it) }))
    }

    fun onExport() {
        subscriptions.add(exporter.exportShoppingList(this.shoppingList.get()?.sl ?: return)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onExportSuccessful, this::showError))
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }

    private fun fetchList(id: String) {
        val sub = manager.getShoppingList(id)
                .doOnSubscribe { showProgress() }
                .doOnNext { hideProgress() }
                .doOnError { hideProgress() }
                .subscribeOn(subscribeOnScheduler)
                .map { VMShoppingList(it) }
                .observeOn(observeOnScheduler)
                .subscribe(this::onShoppingListUpdated, this::showError)
        subscriptions.add(sub)
    }

    private fun fetchItems(list: VMShoppingList) {

        val filter = when (list.mode) {
            ShoppingMode.PREPARATION -> ShoppingListItemsFilter(list.id)
            ShoppingMode.SHOPPING -> ShoppingListItemsFilter(list.id, inList = true)
        }
        this.items.value = Pair(null, mutableListOf())

        itemsSubscription?.dispose()
        itemsSubscription = manager.getShoppingListItems(filter)
                .doOnSubscribe { showProgress() }
                .doOnNext { hideProgress() }
                .subscribeOn(subscribeOnScheduler)
                .map { pair ->
                    val rslt = mutableListOf<VMShoppingListItem>()
                    pair.second.forEach { rslt.add(VMShoppingListItem(it, list.mode)) }
                    return@map Pair(pair.first, rslt)
                }
                .observeOn(observeOnScheduler)
                .subscribe({ onItemsFetched(it) }, { showError(it) })
        subscriptions.add(itemsSubscription!!)
    }

    private fun onExportSuccessful() {
        snackbarData.value = ResIDSnackbarData(R.string.export_successful,
                Snackbar.LENGTH_LONG)
    }

    private fun onShoppingListUpdated(new: VMShoppingList) {
        synchronized(this) {
            val old = shoppingList.get()
            shoppingList.set(new)
            if (old == null || old.mode != new.mode) {
                fetchItems(new)
            }
            onCreateOptionsMenu()
        }
    }

    private fun showError(it: Throwable) {
        snackbarData.value = TextSnackbarData(it, Snackbar.LENGTH_LONG)
    }

    private fun onItemsFetched(changes: Pair<DiffUtil.DiffResult, MutableList<VMShoppingListItem>>) {
        this.items.postValue(changes)
        showRelevantView(changes.second)
    }

    private fun showRelevantView(items: List<VMShoppingListItem>) {
        if (items.isNotEmpty()) {
            showItems()
        } else {
            showNoItemsText()
        }
    }

    private fun showItems() {
        showNoItemsTxt.set(false)
        showFullProgress.set(false)
        showItems.set(true)
    }

    private fun showNoItemsText() {
        showFullProgress.set(false)
        showItems.set(false)
        showNoItemsTxt.set(true)
    }

    private fun showProgress() {
        showItems.set(false)
        showNoItemsTxt.set(false)
        showFullProgress.set(true)
    }

    private fun hideProgress() {
        showFullProgress.set(false)
    }

    class Factory @Inject constructor(
            private val manager: ShoppingManager,
            private val exporter: Exporter,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(manager, exporter, subscribeOnScheduler, observeOnScheduler) as T
        }
    }
}