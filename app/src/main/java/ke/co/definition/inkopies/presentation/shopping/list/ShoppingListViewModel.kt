package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class ShoppingListViewModel @Inject constructor(
        private val manager: ShoppingManager,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val shoppingList = ObservableField<VMShoppingList>()
    val showItems = ObservableField<Boolean>()
    val showNoItemsTxt = ObservableField<Boolean>()
    val showFullProgress = ObservableField<Boolean>()
    val showNextPageLoadingProgress = ObservableField<Boolean>()

    val snackbarData = SingleLiveEvent<SnackBarData>()
    val nextPage = SingleLiveEvent<MutableList<VMShoppingListItem>>()
    val newItem = SingleLiveEvent<VMShoppingListItem>()
    val itemUpdate = SingleLiveEvent<Pair<VMShoppingListItem, Int>>()

    private var currOffset = 0L

    fun start(list: VMShoppingList) {
        this.shoppingList.set(list)
        manager.getShoppingListItems(list.id, currOffset, PRICE_FETCH_COUNT)
                .doOnSubscribe { showProgress() }
                .doOnUnsubscribe { hideProgress() }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map {
                    val rslt = mutableListOf<VMShoppingListItem>()
                    it.forEach { rslt.add(VMShoppingListItem(it, list.mode)) }
                    return@map rslt
                }
                .subscribe({ onNextPageLoad(it) }, { showError(it) })
    }

    fun onItemSelectionChanged(item: VMShoppingListItem, posn: Int, toState: Boolean) {
        var inList = item.inList
        var inCart = item.inCart
        when (item.mode) {
            ShoppingMode.SHOPPING -> inCart = toState
            else -> inList = toState
        }
        manager.updateShoppingListItem(ShoppingListItem(item.id, item.quantity, item.shoppingList,
                item.brandPrice, inList, inCart))
                .doOnSubscribe { item.isUpdating.set(true) }
                .doOnUnsubscribe { item.isUpdating.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingListItem(it, item.mode) }
                .subscribe({ onItemUpdated(it, posn) }, { showError(it) })
    }

    fun onItemAdded(item: VMShoppingListItem) {
        currOffset++
        newItem.value = item
        showItems()
    }

    private fun onItemUpdated(newVal: VMShoppingListItem, posn: Int) {
        itemUpdate.value = Pair(newVal, posn)
    }

    private fun showError(it: Throwable) {
        snackbarData.value = TextSnackBarData(it, Snackbar.LENGTH_LONG)
    }

    private fun onNextPageLoad(page: MutableList<VMShoppingListItem>) {
        nextPage.value = page
        currOffset += page.size
        if (hasItems()) {
            showItems()
        } else {
            showNoItemsText()
        }
    }

    private fun hasItems() = currOffset > 0L

    private fun showItems() {
        showNoItemsTxt.set(false)
        showNextPageLoadingProgress.set(false)
        showFullProgress.set(false)
        showItems.set(true)
    }

    private fun showNoItemsText() {
        showNextPageLoadingProgress.set(false)
        showFullProgress.set(false)
        showItems.set(false)
        showNoItemsTxt.set(true)
    }

    private fun showProgress() {
        showItems.set(false)
        showNoItemsTxt.set(false)
        if (currOffset == 0L) {
            showNextPageLoadingProgress.set(false)
            showFullProgress.set(true)
        } else {
            showFullProgress.set(false)
            showNextPageLoadingProgress.set(true)
        }
    }

    private fun hideProgress() {
        showFullProgress.set(false)
        showNextPageLoadingProgress.set(false)
    }

    class Factory @Inject constructor(
            private val manager: ShoppingManager,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(manager, subscribeOnScheduler, observeOnScheduler) as T
        }
    }

    companion object {
        const val PRICE_FETCH_COUNT = 5
    }
}