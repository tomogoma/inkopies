package ke.co.definition.inkopies.presentation.shopping.lists

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import ke.co.definition.inkopies.model.shopping.ShoppingList

/**
 * Created by tomogoma
 * On 20/03/18.
 */
class ShoppingListsViewModel : ViewModel() {

    val progressShoppingLists = ObservableField<Boolean>()
    val showNoShoppingListsText = ObservableField<Boolean>()
    val showShoppingLists = ObservableField<Boolean>()

    val shoppingLists = MutableLiveData<MutableList<ShoppingList>>()

    fun start() {
        showNoShoppingListsText()
    }

    private fun showShoppingLists() {
        progressShoppingLists.set(false)
        showNoShoppingListsText.set(false)
        showShoppingLists.set(true)
    }

    private fun showProgressShoppingLists() {
        showShoppingLists.set(false)
        showNoShoppingListsText.set(false)
        progressShoppingLists.set(true)
    }

    private fun showNoShoppingListsText() {
        showShoppingLists.set(false)
        progressShoppingLists.set(false)
        showNoShoppingListsText.set(true)
    }
}