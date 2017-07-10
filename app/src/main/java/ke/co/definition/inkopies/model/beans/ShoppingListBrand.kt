package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class ShoppingListBrand : Profile() {
    @Column @NotNull var quantity: Int? = 1
    @Column @NotNull var unitPrice: Float? = 0F
    @ForeignKey(stubbedRelationship = true) var shoppingList: ShoppingList? = null
    @ForeignKey(stubbedRelationship = true) var brand: Brand? = null

    fun getUnitPriceStr(): String {
        return unitPrice.toString()
    }

    fun setUnitPriceStr(unitPriceStr: String) {
        try {
            unitPrice = unitPriceStr.toFloat()
        } catch (e: NumberFormatException) {
            return
        }
    }

    fun getQuantityStr(): String {
        return quantity.toString()
    }

    fun setQuantityStr(quantityStr: String) {
        try {
            quantity = quantityStr.toInt()
        } catch (e: NumberFormatException) {
            return
        }
    }
}
