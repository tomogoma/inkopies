package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.*
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class Brand : Nameable() {
    @Column @Unique @NotNull override var name: String? = null
    @ForeignKey(stubbedRelationship = true) @NotNull var item: Item? = Item()
    @ForeignKey(stubbedRelationship = true) @NotNull var measuringUnit: MeasuringUnit? = item!!.measuringUnit
}
