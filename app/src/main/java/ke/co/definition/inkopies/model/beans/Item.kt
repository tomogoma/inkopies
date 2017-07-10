package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.*
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class Item : Nameable() {
    @Column @NotNull @Unique override var name: String? = null
    @ForeignKey(stubbedRelationship = true) var measuringUnit: MeasuringUnit? = MeasuringUnit()
}
