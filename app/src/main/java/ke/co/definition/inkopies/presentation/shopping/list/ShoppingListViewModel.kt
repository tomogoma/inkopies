package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import ke.co.definition.inkopies.model.shopping.ShoppingList
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class ShoppingListViewModel : ViewModel() {

    val shoppingList = ObservableField<ShoppingList>()

    fun start(shoppingList: ShoppingList) {
        this.shoppingList.set(shoppingList)
    }


    class Factory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel() as T
        }
    }
}