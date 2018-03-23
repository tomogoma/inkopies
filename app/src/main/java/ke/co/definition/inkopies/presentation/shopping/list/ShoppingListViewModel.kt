package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import ke.co.definition.inkopies.model.shopping.ShoppingList

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class ShoppingListViewModel : ViewModel() {

    val ShoppingList = ObservableField<ShoppingList>()
}