package ke.co.definition.inkopies.views

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.model.beans.ShoppingList

class ShoppingListActivity : AppCompatActivity(), ShoppingListPlanFragment.PriceSettable {

    companion object {

        private val EXTRA_SHOPPING_LIST = ShoppingListActivity::class.java.name + "EXTRA_SHOPPING_LIST"

        fun start(a: Activity, sl: ShoppingList) {
            val i = Intent(a, ShoppingListActivity::class.java)
            i.putExtra(EXTRA_SHOPPING_LIST, sl)
            a.startActivity(i)
        }
    }

    private var binding: ActivityShoppingListBinding? = null
    private var currFragment: ShoppingListPlanFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shopping_list)
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding!!.fab.setOnClickListener { _ ->
            currFragment?.newShoppingListBrand()
        }
        val sl = intent.getSerializableExtra(EXTRA_SHOPPING_LIST) as ShoppingList
        currFragment = ShoppingListPlanFragment.initialize(sl)
        binding!!.title.text = sl.name
        supportFragmentManager.beginTransaction()
                .add(R.id.frame, currFragment)
                .commit()
    }

    override fun setPrice(total_selected: Pair<Float, Float>) {
        val totalPriceStr = getString(R.string.total_price_title_fmt)
        binding!!.toolbarStartText.text = String.format(totalPriceStr, total_selected.second)
    }

}
