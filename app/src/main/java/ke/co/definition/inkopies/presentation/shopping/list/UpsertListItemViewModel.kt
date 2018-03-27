package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.clearErrorOnChange
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 27/03/18.
 */

class UpsertListItemViewModel : ViewModel() {

    var brandName = ObservableField<String>()
    var itemName = ObservableField<String>()
    var quantity = ObservableField<String>()
    var measuringUnit = ObservableField<String>()
    var unitPrice = ObservableField<String>()
    var brandNameError = ObservableField<String>()
    var itemNameError = ObservableField<String>()
    var quantityError = ObservableField<String>()
    var measuringUnitError = ObservableField<String>()
    var unitPriceError = ObservableField<String>()
    var overlayProgress = ObservableField<ProgressData>()

    init {
        brandName.clearErrorOnChange(brandNameError)
        itemName.clearErrorOnChange(brandNameError)
        quantity.clearErrorOnChange(brandNameError)
        measuringUnit.clearErrorOnChange(measuringUnitError)
        unitPrice.clearErrorOnChange(brandNameError)
    }

    fun start(item: VMShoppingListItem?) {
        if (item == null) {
            return
        }
        brandName.set(item.brandName())
        itemName.set(item.itemName())
        quantity.set(item.quantity.toString())
        measuringUnit.set(item.measuringUnitName())
        unitPrice.set(item.unitPrice().toString())
    }

    fun onDelete() {
        TODO()
    }

    fun onSubmit() {
        TODO()
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UpsertListItemViewModel() as T
        }
    }
}