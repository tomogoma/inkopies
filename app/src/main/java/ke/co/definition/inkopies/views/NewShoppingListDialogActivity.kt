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

        private val EXTRA_REASON: String = NewShoppingListDialogActivity::class.java.name

        fun start(a: Activity) {
            val i = Intent(a, NewShoppingListDialogActivity::class.java)
            a.startActivity(i)
        }

        fun startForReason(a: Activity, reasonResource: Int) {
            val i = Intent(a, NewShoppingListDialogActivity::class.java)
            i.putExtra(NewShoppingListDialogActivity.EXTRA_REASON, reasonResource)
            a.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_shopping_list_dialog)
        binding?.shoppingList = ShoppingList()
        val reasonResource = intent.getIntExtra(EXTRA_REASON, -1)
        if (reasonResource > 0) {
            binding?.reason?.setText(reasonResource)
            binding?.reason?.visibility = View.VISIBLE
        }
    }

    fun createShoppingList(v: View) {
        if (!validate()) {
            return
        }
        Model.newShoppingList((binding as ActivityNewShoppingListDialogBinding).shoppingList)
        finish()
    }

    fun validate(): Boolean {
        if (binding?.shoppingList?.name == null) {
            binding?.name?.error = getString(R.string.field_required)
            return false
        }
        return true
    }
}
