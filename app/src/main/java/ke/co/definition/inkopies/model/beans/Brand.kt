package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class Brand : Profile() {
    @Column var name: String? = null
    @ForeignKey(stubbedRelationship = true) @NotNull var item: Item? = null
    @ForeignKey(stubbedRelationship = true) @NotNull var measuringUnit: MeasuringUnit? = item?.measuringUnit
}
