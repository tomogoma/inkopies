package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.shopping.ShoppingListItemRequest
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.presentation.common.clearErrorOnChange
import ke.co.definition.inkopies.presentation.shopping.common.Nameable
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

    val title = ObservableField<Int>()
    val brandName = ObservableField<String>()
    val itemName = ObservableField<String>()
    val quantity = ObservableField<String>()
    val measuringUnit = ObservableField<String>()
    val unitPrice = ObservableField<String>()
    val brandNameError = ObservableField<String>()
    val itemNameError = ObservableField<String>()
    val quantityError = ObservableField<String>()
    val measuringUnitError = ObservableField<String>()
    val unitPriceError = ObservableField<String>()
    val overlayProgress = ObservableField<ProgressData>()
    val deletable = ObservableField<Boolean>()

    val snackBarData = SingleLiveEvent<SnackBarData>()
    val finished = SingleLiveEvent<VMShoppingListItem>()
    val searchItemNameResult = SingleLiveEvent<List<Nameable>>()
    val searchBrandNameResult = SingleLiveEvent<List<Nameable>>()
    val searchQuantityResult = SingleLiveEvent<List<Nameable>>()
    val searchMeasuringUnitResult = SingleLiveEvent<List<Nameable>>()
    val searchUnitPriceResult = SingleLiveEvent<List<Nameable>>()

    private lateinit var list: VMShoppingList
    private var id: String? = null

    init {
        brandName.clearErrorOnChange(brandNameError)
        itemName.clearErrorOnChange(brandNameError)
        quantity.clearErrorOnChange(brandNameError)
        measuringUnit.clearErrorOnChange(measuringUnitError)
        unitPrice.clearErrorOnChange(brandNameError)
    }

    fun start(list: VMShoppingList, item: VMShoppingListItem?) {
        this.list = list
        if (item == null) {
            title.set(R.string.new_item_title)
            deletable.set(false)
            return
        }
        title.set(R.string.edit_item_title)
        deletable.set(true)
        id = item.id
        brandName.set(item.brandName())
        itemName.set(item.itemName())
        quantity.set(item.quantity.toString())
        measuringUnit.set(item.measuringUnitName())
        unitPrice.set(item.unitPrice().toString())
    }

    class Value(val value: String) : Nameable {
        override fun name(): String {
            return value
        }
    }

    fun onSearchItemName(search: String) {
        TODO()
    }

    fun onSearchBrandName(search: String) {
        TODO()
    }

    fun onSearchQuantity(search: String) {
        TODO()
    }

    fun onSearchMeasuringUnit(search: String) {
        TODO()
    }

    fun onSearchUnitPrice(search: String) {
        TODO()
    }

    fun onDelete() {
        manager.deleteShoppingListItem(id!!)
                .doOnSubscribe {
                    overlayProgress.set(ProgressData(resMan.getString(R.string.saving_item)))
                }
                .doOnUnsubscribe { overlayProgress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ finished.value = null }, {
                    snackBarData.value = TextSnackBarData(it, Snackbar.LENGTH_LONG)
                })
    }

    fun onSubmit() {
        if (!validate()) {
            return
        }
        // TODO implement in UI (inList and inCart) e.g. have an is checked option
        val inList = true
        val inCart = list.mode == ShoppingMode.SHOPPING
        manager.upsertShoppingListItem(ShoppingListItemRequest(
                list.id, itemName.get()!!, inList, inCart, id, brandName.get(), quantity.get()?.toInt(),
                measuringUnit.get(), unitPrice.get()?.toFloat()
        ))
                .doOnSubscribe {
                    overlayProgress.set(ProgressData(resMan.getString(R.string.saving_item)))
                }
                .doOnUnsubscribe { overlayProgress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .map { VMShoppingListItem(it, list.mode) }
                .subscribe({ finished.value = it }, {
                    snackBarData.value = TextSnackBarData(it, Snackbar.LENGTH_LONG)
                })
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