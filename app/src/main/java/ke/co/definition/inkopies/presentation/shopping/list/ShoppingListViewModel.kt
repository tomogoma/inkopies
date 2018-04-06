package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.presentation.common.ResIDSnackbarData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
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

    val snackbarData = SingleLiveEvent<SnackbarData>()
    val nextPage = SingleLiveEvent<MutableList<VMShoppingListItem>>()
    val newItem = SingleLiveEvent<VMShoppingListItem>()
    val itemUpdate = SingleLiveEvent<Pair<VMShoppingListItem, Int>>()
    val itemDelete = SingleLiveEvent<Int>()
    val menuRes = SingleLiveEvent<Int>()
    val clearList = SingleLiveEvent<Boolean>()

    private var currOffset = 0L

    fun start(list: VMShoppingList) {
        this.shoppingList.set(list)
        currOffset = 0L
        clearList.value = true
        val filter = when (list.mode) {
            ShoppingMode.PREPARATION -> ShoppingListItemsFilter(list.id)
            ShoppingMode.SHOPPING -> ShoppingListItemsFilter(list.id, inList = true)
        }
        manager.getShoppingListItems(filter, currOffset, PRICE_FETCH_COUNT)
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

    fun onCreateOptionsMenu() {
        menuRes.value = when (shoppingList.get()!!.mode) {
            ShoppingMode.PREPARATION -> R.menu.planning_main_menu
            ShoppingMode.SHOPPING -> R.menu.shopping_main_menu
        }
    }

    fun onCheckout() {
        // TODO("Perform checkout")
        snackbarData.value = ResIDSnackbarData(R.string.feature_not_implemented, Snackbar.LENGTH_LONG)
    }

    fun onChangeMode(toMode: ShoppingMode) {
        val list = shoppingList.get()!!
        manager.updateShoppingList(ShoppingList(list.id, list.name(), list.sl.activeListPrice,
                list.sl.cartPrice, toMode))
                .doOnSubscribe { forceShowFullProgress() }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingList(it) }
                .subscribe(this::onShoppingListUpdated, this::showError)
    }

    fun onItemSelectionChanged(item: VMShoppingListItem, posn: Int, toState: Boolean) {
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
        val list = shoppingList.get()!!
        manager.updateShoppingListItem(ShoppingListItemUpdate(list.id, item.id,
                inList = inList, inCart = inCart))
                .doOnSubscribe { item.isUpdating.set(true) }
                .doOnUnsubscribe { item.isUpdating.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingListItem(it, item.mode) }
                .subscribe({ onItemUpdated(item, it, posn) }, { showError(it) })
    }

    fun onItemAdded(item: VMShoppingListItem) {
        currOffset++
        shoppingList.set(shoppingList.get()!!.accumulateInsertPrices(item))
        newItem.value = item
        showItems()
    }

    fun onItemUpdated(old: VMShoppingListItem, new: VMShoppingListItem, posn: Int) {
        val list = shoppingList.get()!!
        shoppingList.set(list.accumulateUpdatePrices(list.id, old, new))
        itemUpdate.value = Pair(new, posn)
    }

    fun onItemDeleted(item: VMShoppingListItem, posn: Int) {
        currOffset--
        shoppingList.set(shoppingList.get()!!.accumulateDeletePrices(item))
        itemDelete.value = posn
    }

    private fun onShoppingListUpdated(list: VMShoppingList) {
        start(list)
        onCreateOptionsMenu()
    }

    private fun showError(it: Throwable) {
        snackbarData.value = TextSnackbarData(it, Snackbar.LENGTH_LONG)
    }

    private fun onNextPageLoad(page: MutableList<VMShoppingListItem>) {
        nextPage.value = page
        currOffset += page.size
        showItemsIfHasItems()
    }

    private fun showItemsIfHasItems() {
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

    private fun forceShowFullProgress() {
        showItems.set(false)
        showNoItemsTxt.set(false)
        showNextPageLoadingProgress.set(false)
        showFullProgress.set(true)
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