package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.Unique
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class MeasuringUnit(@Column @NotNull @Unique override var name: String? = null) : Nameable()
