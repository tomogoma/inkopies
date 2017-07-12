package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class Brand : Nameable() {

    @Column @NotNull
    override var name: String? = null

    @Column @NotNull
    var unitPrice: Float? = 0F

    @NotNull
    @ForeignKey(stubbedRelationship = true)
    var item: Item? = Item()

    @ForeignKey(stubbedRelationship = true)
    @NotNull var measuringUnit: MeasuringUnit? = item!!.measuringUnit

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
}
