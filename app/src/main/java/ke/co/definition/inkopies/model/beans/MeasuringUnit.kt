package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

@Table(database = DB::class)
class MeasuringUnit(@PrimaryKey override var name: String? = null) : Nameable()
