package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
open class ShoppingListBrand() : Profile() {

    companion object {
        val STATUS_INACTIVE = 0
        val STATUS_SCHEDULED = 1
        val STATUS_CARTED = 2
    }

    @Column @NotNull var quantity: Int? = 1
    @Column @NotNull var status: Int? = STATUS_SCHEDULED
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

    fun isStatusBoxChecked(): Boolean = when (shoppingList?.currMode) {
        ShoppingList.SHOPPING -> isCarted()
        else -> isScheduled()
    }

    fun setStatusBoxChecked(checked: Boolean) = when (shoppingList?.currMode) {
        ShoppingList.SHOPPING -> setCarted(checked)
        else -> setScheduled(checked)
    }

    fun isScheduled() = status!! >= STATUS_SCHEDULED

    fun isCarted() = status!! >= STATUS_CARTED

    fun setScheduled(scheduled: Boolean) {
        if (scheduled) {
            if (status!! < STATUS_SCHEDULED) {
                status = STATUS_SCHEDULED
            }
        } else {
            status = STATUS_INACTIVE
        }
    }

    fun setCarted(carted: Boolean) {
        if (carted) {
            status = STATUS_CARTED
        } else if (status!! >= STATUS_CARTED) {
            status = STATUS_SCHEDULED
        }
    }
}
