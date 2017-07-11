package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.*
import ke.co.definition.inkopies.model.db.DB

@Table(
        database = DB::class,
        uniqueColumnGroups = arrayOf(
                UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL)
        )
)
class Brand : Nameable() {

    @Column @NotNull
    @Unique(unique = false, uniqueGroups = intArrayOf(1))
    override var name: String? = null

    @Column @NotNull
    var unitPrice: Float? = 0F

    @NotNull
    @ForeignKey(stubbedRelationship = true)
    @Unique(unique = false, uniqueGroups = intArrayOf(1))
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
