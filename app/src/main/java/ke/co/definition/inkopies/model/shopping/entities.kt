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
        val name: String
)

data class Brand(
        val id: String,
        val name: String,
        val measuringUnit: MeasuringUnit,
        val shoppingItem: ShoppingItem
) {
    fun measuringUnitName() = measuringUnit.name
    fun itemName() = shoppingItem.name
}

data class BrandPrice(
        val id: String,
        val price: Float,
        val brand: Brand,
        val atStoreBranch: StoreBranch
) {
    fun measuringUnitName() = brand.measuringUnitName()
    fun itemName() = brand.itemName()
    fun brandName() = brand.name
}

data class ShoppingListItem(
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

data class ShoppingListItemUpsert(
        val shoppingListID: String,
        val itemName: String,
        val inList: Boolean,
        val inCart: Boolean,
        val shoppingListItemID: String?,
        val brandName: String?,
        val quantity: Int?,
        val measuringUnit: String?,
        val unitPrice: Float?
)

data class ShoppingListItemSearch(
        val brandName: String?,
        val shoppingItemName: String?,
        val brandPrice: String?,
        val measUnit: String?
)

enum class ShoppingMode {
    PREPARATION, SHOPPING
}

data class ShoppingList(
        val id: String,
        val name: String,
        val activeListPrice: Float,
        val cartPrice: Float,
        val mode: ShoppingMode = ShoppingMode.PREPARATION
)