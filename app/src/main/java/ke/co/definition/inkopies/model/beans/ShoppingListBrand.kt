package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
open class ShoppingListBrand() : Profile() {
    @Column @NotNull var quantity: Int? = 1
    @ForeignKey(stubbedRelationship = true) var shoppingList: ShoppingList? = null
    @ForeignKey(stubbedRelationship = true) var brand: Brand? = Brand()

    constructor(sl: ShoppingList) : this() {
        shoppingList = sl
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
