package ke.co.definition.inkopies.views

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityNewShoppingListDialogBinding
import ke.co.definition.inkopies.model.Model
import ke.co.definition.inkopies.model.beans.ShoppingList

class NewShoppingListDialogActivity : AppCompatActivity() {

    private var binding: ActivityNewShoppingListDialogBinding? = null

    companion object {
        fun start(a: Activity) {
            val i = Intent(a, NewShoppingListDialogActivity::class.java)
            a.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_shopping_list_dialog)
        binding?.shoppingList = ShoppingList()
    }

    fun createShoppingList(v: View) {
        Model.newShoppingList((binding as ActivityNewShoppingListDialogBinding).shoppingList)
    }
}
