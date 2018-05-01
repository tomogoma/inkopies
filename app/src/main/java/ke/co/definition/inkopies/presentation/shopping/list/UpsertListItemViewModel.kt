package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.presentation.common.*
import ke.co.definition.inkopies.presentation.shopping.common.SearchShoppingListItemResult
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 27/03/18.
 */

class UpsertListItemViewModel @Inject constructor(
        private val manager: ShoppingManager,
        private val resMan: ResourceManager,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val title = ObservableInt()
    val brandName = ObservableField<String>()
    val categoryName = ObservableField<String>()
    val itemName = ObservableField<String>()
    val quantity = ObservableField<String>()
    val measuringUnit = ObservableField<String>()
    val unitPrice = ObservableField<String>()
    val brandNameError = ObservableField<String>()
    val categoryNameError = ObservableField<String>()
    val itemNameError = ObservableField<String>()
    val quantityError = ObservableField<String>()
    val measuringUnitError = ObservableField<String>()
    val unitPriceError = ObservableField<String>()
    val overlayProgress = ObservableField<ProgressData>()
    val deletable = ObservableBoolean()
    val checked = ObservableBoolean()
    val addAnother = ObservableBoolean()
    val checkedText = ObservableField<String>()

    val snackBarData = SingleLiveEvent<SnackbarData>()
    val finished = SingleLiveEvent<VMShoppingListItem>()
    val finishedAddNext = SingleLiveEvent<VMShoppingListItem>()
    val searchItemNameResult = SingleLiveEvent<List<SearchShoppingListItemResult>>()
    val searchBrandNameResult = SingleLiveEvent<List<SearchShoppingListItemResult>>()
    val searchMeasuringUnitResult = SingleLiveEvent<List<SearchShoppingListItemResult>>()
    val searchUnitPriceResult = SingleLiveEvent<List<SearchShoppingListItemResult>>()
    val searchCategoryResult = SingleLiveEvent<List<String>>()

    private lateinit var list: VMShoppingList
    private var id: String? = null

    init {
        categoryName.clearErrorOnChange(categoryNameError)
        brandName.clearErrorOnChange(brandNameError)
        itemName.clearErrorOnChange(brandNameError)
        quantity.clearErrorOnChange(brandNameError)
        measuringUnit.clearErrorOnChange(measuringUnitError)
        unitPrice.clearErrorOnChange(brandNameError)
    }

    fun start(list: VMShoppingList, item: VMShoppingListItem?) {

        this.list = list
        checkedText.set(if (list.mode == ShoppingMode.PREPARATION) {
            resMan.getString(R.string.add_to_list)
        } else {
            resMan.getString(R.string.add_to_cart)
        })
        clearFields()

        if (item == null) {
            title.set(R.string.new_item_title)
            deletable.set(false)
            checked.set(true)
            return
        }

        title.set(R.string.edit_item_title)
        deletable.set(true)
        onChangeShoppingListItem(item)
    }

    fun onChangeShoppingListItem(item: VMShoppingListItem) {
        id = item.id
        categoryName.set(item.categoryName())
        brandName.set(item.brandName())
        itemName.set(item.itemName())
        quantity.set(item.quantity.toString())
        measuringUnit.set(item.measuringUnitName())
        unitPrice.set(item.unitPrice().toString())
        checked.set(item.isChecked())
    }

    fun onSearchItemName(search: String) {
        val req = ShoppingListItemSearch(brandName.get(), search, unitPrice.get(),
                measuringUnit.get())
        search(req, mapFunc = {
            return@search Pair(
                    it.itemName(),
                    SearchShoppingListItemResult("${it.itemName()} ::. ${it.brandName()} ",
                            VMShoppingListItem(it, list.mode))
            )
        }, successFunc = { searchItemNameResult.value = it })
    }

    fun onSearchBrandName(search: String) {
        val req = ShoppingListItemSearch(search, itemName.get(), unitPrice.get(),
                measuringUnit.get())
        search(req, mapFunc = {
            return@search Pair(
                    it.brandName(),
                    SearchShoppingListItemResult("${it.brandName()} ::. ${it.itemName()}",
                            VMShoppingListItem(it, list.mode))
            )
        }, successFunc = { searchBrandNameResult.value = it })
    }

    fun onSearchCategory(search: String) {
        manager.searchCategory(search)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { it.map { it.name } }
                .subscribe({ searchCategoryResult.value = it }, { onOpException(it) })
    }

    fun onSearchMeasuringUnit(search: String) {
        val req = ShoppingListItemSearch(brandName.get(), itemName.get(), unitPrice.get(), search)
        search(req, mapFunc = {
            return@search Pair(
                    it.measuringUnitName(),
                    SearchShoppingListItemResult(
                            "${it.measuringUnitName()} ::. ${it.brandName()} ${it.itemName()}",
                            VMShoppingListItem(it, list.mode)
                    )
            )
        }, successFunc = { searchMeasuringUnitResult.value = it })
    }

    fun onSearchUnitPrice(search: String) {
        val req = ShoppingListItemSearch(brandName.get(), itemName.get(), search,
                measuringUnit.get())
        search(req, mapFunc = {
            val unitPrice = it.unitPrice().formatPrice()
            return@search Pair(
                    unitPrice,
                    SearchShoppingListItemResult(
                            "$unitPrice::. ${it.brandName()} ${it.itemName()}",
                            VMShoppingListItem(it, list.mode)
                    )
            )
        }, successFunc = { searchUnitPriceResult.value = it })
    }

    fun onDelete() {
        manager.deleteShoppingListItem(list.id, id!!)
                .doOnSubscribe {
                    overlayProgress.set(ProgressData(resMan.getString(R.string.saving_item)))
                }
                .doOnUnsubscribe { overlayProgress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ finished.value = null }, { onOpException(it) })
    }

    fun onSubmit() {

        if (!validate()) {
            return
        }
        val inListCart = getInListInCart()

        val currID = id
        if (currID == null || currID == "") {
            manager.insertShoppingListItem(ShoppingListItemInsert(list.id, itemName.get()!!,
                    inListCart.first, inListCart.second, categoryName.get(), brandName.get(),
                    quantity.get()?.toIntOrNull(), measuringUnit.get(),
                    unitPrice.get()?.toFloatOrNull()))
        } else {
            manager.updateShoppingListItem(ShoppingListItemUpdate(list.id, currID, itemName.get()!!,
                    inListCart.first, inListCart.second, categoryName.get(), brandName.get(),
                    quantity.get()?.toIntOrNull(), measuringUnit.get(),
                    unitPrice.get()?.toFloatOrNull()))
        }
                .doOnSubscribe {
                    overlayProgress.set(ProgressData(resMan.getString(R.string.saving_item)))
                }
                .doOnUnsubscribe { overlayProgress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingListItem(it, list.mode) }
                .subscribe(this::onAdded, this::onOpException)
    }

    private fun onAdded(item: VMShoppingListItem) {
        if (addAnother.get()) {
            finishedAddNext.value = item
            clearFields()
        } else {
            finished.value = item
        }
    }

    private fun clearFields() {
        categoryName.set("")
        brandName.set("")
        itemName.set("")
        quantity.set("1")
        measuringUnit.set("")
        unitPrice.set("")
    }

    private fun getInListInCart(): Pair<Boolean, Boolean> {
        return if (list.mode == ShoppingMode.PREPARATION) {
            Pair(/*inList*/checked.get(), /*inCart*/false)
        } else {
            Pair(/*inList*/true, /*inCart*/checked.get())
        }
    }

    private fun search(req: ShoppingListItemSearch,
                       mapFunc: (ShoppingListItem) -> Pair<String, SearchShoppingListItemResult>,
                       successFunc: (MutableList<SearchShoppingListItemResult>) -> Unit) {

        manager.searchShoppingListItem(req)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map {
                    val rslt = mutableListOf<SearchShoppingListItemResult>()
                    it.forEach {
                        val mapped = mapFunc(it)
                        rslt.add(SearchShoppingListItemResult(mapped.first))
                        rslt.add(mapped.second)
                    }
                    return@map rslt
                }
                .subscribe({ successFunc(it) }, { onOpException(it) })
    }

    private fun onOpException(e: Throwable) {
        snackBarData.value = TextSnackbarData(e, Snackbar.LENGTH_LONG)
    }

    private fun validate(): Boolean {
        var isValid = true
        val item = itemName.get()
        if (item == null || item == "") {
            itemNameError.set(resMan.getString(R.string.error_required_field))
            isValid = false

        }
        return isValid
    }

    class Factory @Inject constructor(
            private val manager: ShoppingManager,
            private val resMan: ResourceManager,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UpsertListItemViewModel(manager, resMan, subscribeOnScheduler, observeOnScheduler)
                    as T
        }
    }
}