package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.Unique
import ke.co.definition.inkopies.model.db.DB

/**
 * Created by tomogoma on 08/07/17.
 */

@Table(database = DB::class)
class ShoppingList : Nameable() {

    enum class Mode {
        PLANNING, SHOPPING
    }

    @Column @NotNull @Unique override var name: String? = null
    @NotNull var currMode: Mode? = Mode.PLANNING
}
