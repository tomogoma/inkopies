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
data class ShoppingList(@Column @NotNull @Unique var name: String? = null) : Profile()
