package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.NotNull
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import ke.co.definition.inkopies.model.db.DB

/**
 * Created by tomogoma on 08/07/17.
 */

@Table(database = DB::class)
class ShoppingList : Nameable() {

    enum class Mode {
        PLANNING, SHOPPING
    }

    @PrimaryKey override var name: String? = null
    @NotNull var currMode: Mode? = Mode.PLANNING
}
