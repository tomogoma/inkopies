package ke.co.definition.inkopies.model.shopping

import ke.co.definition.inkopies.model.stores.StoreBranch

/**
 * Created by tomogoma
 * On 25/03/18.
 */

data class MeasuringUnit(
        val id: String,
        val name: String
)

data class ShoppingItem(
        val id: String,
        val name: String,
        val measuringUnit: MeasuringUnit
)

data class Brand(
        val id: String,
        val name: String,
        val measuringUnit: MeasuringUnit,
        val shoppingItem: ShoppingItem? = null
) {
    fun effectiveMeasuringUnitName() = effectiveMeasuringUnit().name
    fun effectiveMeasuringUnit() = shoppingItem?.measuringUnit ?: measuringUnit
    fun itemName() = shoppingItem?.name ?: ""
}

data class BrandPrice(
        val id: String,
        val price: Float,
        val brand: Brand,
        val atStoreBranch: StoreBranch
) {
    fun measuringUnitName() = brand.effectiveMeasuringUnitName()
    fun itemName() = brand.itemName()
    fun brandName() = brand.name
}

open class ShoppingListItem(
        val id: String,
        val quantity: Int,
        val shoppingList: ShoppingList,
        val brandPrice: BrandPrice,
        val inList: Boolean,
        val inCart: Boolean
) {
    fun measuringUnitName() = brandPrice.measuringUnitName()
    fun itemName() = brandPrice.itemName()
    fun brandName() = brandPrice.brandName()
    fun unitPrice() = brandPrice.price
    fun totalPrice() = unitPrice() * quantity
}

enum class ShoppingMode {
    PREPARATION, SHOPPING
}

open class ShoppingList(
        val id: String,
        val name: String,
        val activeListPrice: Float,
        val cartPrice: Float,
        val mode: ShoppingMode = ShoppingMode.PREPARATION
)