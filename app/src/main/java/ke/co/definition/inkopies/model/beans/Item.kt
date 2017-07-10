package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.*
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class Item : Profile() {
    @Column @NotNull @Unique var name: String? = null
    @ForeignKey(stubbedRelationship = true) var measuringUnit: MeasuringUnit? = null
}
