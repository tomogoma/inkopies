package ke.co.definition.inkopies.views

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.model.beans.ShoppingList

class ShoppingListActivity : AppCompatActivity() {

    companion object {

        private val EXTRA_SHOPPING_LIST = ShoppingListActivity::class.java.name + "EXTRA_SHOPPING_LIST"

        fun start(a: Activity, sl: ShoppingList) {
            val i = Intent(a, ShoppingListActivity::class.java)
            i.putExtra(EXTRA_SHOPPING_LIST, sl)
            a.startActivity(i)
        }
    }

    private var currFragment: ShoppingListPlanFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityShoppingListBinding>(this, R.layout.activity_shopping_list)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.fab.setOnClickListener { _ ->
            currFragment?.newShoppingListBrand()
        }
        val sl = intent.getSerializableExtra(EXTRA_SHOPPING_LIST) as ShoppingList
        currFragment = ShoppingListPlanFragment.initialize(sl)
        supportFragmentManager.beginTransaction()
                .add(R.id.frame, currFragment)
                .commit()
    }

}
