package ke.co.definition.inkopies.presentation.shopping.common

import androidx.databinding.ObservableBoolean
import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.model.shopping.ShoppingListItemUpdate
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.presentation.common.formatPrice

/**
 * Created by tomogoma
 * On 25/03/18.
 */
class VMShoppingList(val sl: ShoppingList) {

    val id = sl.id
    val mode = sl.mode

    fun getFmtActiveListPrice() = sl.activeListPrice.formatPrice()
    fun getFmtCartPrice() = sl.cartPrice.formatPrice()
    fun isShowActiveListPrice() = sl.activeListPrice > 0
    fun isShowCartPrice() = sl.cartPrice > 0
    fun name() = sl.name

    fun accumulateInsertPrices(toInsert: VMShoppingListItem) =
            VMShoppingList(sl.accumulateInsertPrices(toInsert.totalPrice(), toInsert.inList,
                    toInsert.inCart))

    fun accumulateDeletePrices(toDelete: VMShoppingListItem) =
            VMShoppingList(sl.accumulateDeletePrices(toDelete.sli))

    fun accumulateUpdatePrices(shoppingListID: String, old: VMShoppingListItem, new: VMShoppingListItem) =
            VMShoppingList(sl.accumulateUpdatePrices(old.sli, ShoppingListItemUpdate(
                    shoppingListID, new.id, new.itemName(), new.inList, new.inCart,
                    new.categoryName(), new.brandName(), new.quantity, new.measuringUnitName(),
                    new.unitPrice()
            )))

}

class VMShoppingListItem(val sli: ShoppingListItem, val mode: ShoppingMode) {

    var isUpdating = ObservableBoolean(false)
    val id: String = sli.id
    val inList: Boolean = sli.inList
    val inCart: Boolean = sli.inCart
    val quantity: Int = sli.quantity

    fun isChecked() = (mode == ShoppingMode.PREPARATION && sli.inList) || (mode == ShoppingMode.SHOPPING && sli.inCart)
    fun fmtUnitPrice() = sli.unitPrice().formatPrice()
    fun fmtTotalPrice() = sli.totalPrice().formatPrice()
    fun measuringUnitName() = sli.measuringUnitName()
    fun itemName() = sli.itemName()
    fun brandName() = sli.brandName()
    fun fmtQuantity() = quantity.toString()
    fun unitPrice() = sli.unitPrice()
    fun totalPrice() = sli.totalPrice()
    fun categoryName() = sli.categoryName()
    fun isShowBrandName() = sli.brandName().isNotBlank()
    fun isShowUnitPrice() = sli.unitPrice() > 0
    fun isShowMeasUnit() = sli.measuringUnitName().isNotBlank()
    fun isShowUnitPriceRow() = isShowUnitPrice() || isShowMeasUnit()
    fun isShowQuantity() = quantity > 1
    fun isShowTotalPriceRow() = isShowQuantity() && isShowUnitPrice()
    fun isShowCategory() = sli.categoryName().isNotBlank()
}

data class SearchShoppingListItemResult(
        private val printName: String,
        val sli: VMShoppingListItem? = null
) {
    override fun toString() = printName
}