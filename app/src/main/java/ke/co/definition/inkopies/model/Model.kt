package ke.co.definition.inkopies.model

import android.content.Context
import com.raizlabs.android.dbflow.config.FlowManager
import ke.co.definition.inkopies.model.beans.ShoppingList

/**
 * Created by tomogoma on 08/07/17.
 */

class Model {
    companion object {
        fun init(c: Context) {
            FlowManager.init(c)
        }
    }
    fun newShoppingList(sl: ShoppingList) = sl.save()
}
