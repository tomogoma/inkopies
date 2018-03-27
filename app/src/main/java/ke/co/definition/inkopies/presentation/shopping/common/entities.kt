package ke.co.definition.inkopies.presentation.shopping.common

import android.databinding.ObservableField
import ke.co.definition.inkopies.model.shopping.BrandPrice
import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
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
}

class VMShoppingListItem(val sli: ShoppingListItem, val mode: ShoppingMode) {

    var isUpdating = ObservableField<Boolean>(false)
    val id: String = sli.id
    val inList: Boolean = sli.inList
    val inCart: Boolean = sli.inCart
    val shoppingList: ShoppingList = sli.shoppingList
    val brandPrice: BrandPrice = sli.brandPrice
    val quantity: Int = sli.quantity

    fun isChecked() = (mode == ShoppingMode.PREPARATION && sli.inList) || (mode == ShoppingMode.SHOPPING && sli.inCart)
    fun fmtUnitPrice() = sli.unitPrice().formatPrice()
    fun fmtTotalPrice() = sli.totalPrice().formatPrice()
    fun measuringUnitName() = sli.measuringUnitName()
    fun itemName() = sli.itemName()
    fun brandName() = sli.brandName()
    fun fmtQuantity() = quantity.toString()
    fun unitPrice() = sli.unitPrice()
}