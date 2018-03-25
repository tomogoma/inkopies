package ke.co.definition.inkopies.presentation.shopping.common

import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.presentation.common.formatPrice

/**
 * Created by tomogoma
 * On 25/03/18.
 */
class VMShoppingList(sl: ShoppingList) : ShoppingList(
        sl.id, sl.name, sl.activeListPrice, sl.cartPrice, sl.mode
) {
    fun getFmtActiveListPrice() = activeListPrice.formatPrice()
    fun getFmtCartPrice() = cartPrice.formatPrice()
    fun isShowActiveListPrice() = activeListPrice > 0
    fun isShowCartPrice() = cartPrice > 0
}

class VMShoppingListItem(sli: ShoppingListItem, val mode: ShoppingMode) : ShoppingListItem(
        sli.id, sli.quantity, sli.shoppingList, sli.brandPrice, sli.inList, sli.inCart
) {

    fun isChecked() = (mode == ShoppingMode.PREPARATION && inList) || (mode == ShoppingMode.SHOPPING && inCart)
    fun formattedUnitPrice() = unitPrice().formatPrice()
    fun formattedTotalPrice() = totalPrice().formatPrice()
}