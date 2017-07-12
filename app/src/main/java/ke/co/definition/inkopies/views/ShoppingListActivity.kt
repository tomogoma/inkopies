package ke.co.definition.inkopies.views

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.model.Model
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

    private lateinit var sl: ShoppingList
    private lateinit var binding: ActivityShoppingListBinding
    private lateinit var currFragment: ShoppingListPlanFragment
    private lateinit var goShopping: MenuItem
    private lateinit var checkout: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sl = intent.getSerializableExtra(EXTRA_SHOPPING_LIST) as ShoppingList
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shopping_list)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.fab.setOnClickListener { _ ->
            currFragment.newShoppingListBrand()
        }
        currFragment = ShoppingListPlanFragment.initialize(sl)
        supportFragmentManager.beginTransaction()
                .add(R.id.frame, currFragment)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.plan_shopping, menu)
        goShopping = menu.findItem(R.id.goShopping)
        checkout = menu.findItem(R.id.checkout)
        when (sl.currMode) {
            ShoppingList.SHOPPING -> menuModeShopping()
            else -> menuModePlanning()
        }
        binding.toolbar.title = sl.name
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.goShopping -> initiateGoShopping()
            R.id.checkout -> initiatePlanning()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun setPrice(totals: Pair<Float, Float>) {
        val startStr: String
        val endStr: String
        val totalFmt = getString(R.string.total_price_title_fmt)
        when (sl.currMode) {
            ShoppingList.SHOPPING -> {
                val cartFmt = getString(R.string.cart_price_title_fmt)
                startStr = String.format(totalFmt, totals.first)
                endStr = String.format(cartFmt, totals.second)
            }
            else -> {
                startStr = String.format(getString(R.string.total_price_title_fmt), totals.second)
                endStr = ""
            }
        }
        binding.toolbarStartText.text = startStr
        binding.toolbarEndText.text = endStr
    }

    private fun initiateGoShopping() {
        sl.currMode = ShoppingList.SHOPPING
        Model.upsertShoppingList(sl)
        menuModeShopping()
        currFragment.loadList(sl)
    }

    private fun initiatePlanning() {
        sl.currMode = ShoppingList.PLANNING
        Model.upsertShoppingList(sl)
        menuModePlanning()
        currFragment.loadList(sl)
    }

    private fun menuModeShopping() {
        goShopping.isVisible = false
        checkout.isVisible = true
        binding.toolbar.subtitle = getString(R.string.shopping_title)
    }

    private fun menuModePlanning() {
        checkout.isVisible = false
        goShopping.isVisible = true
        binding.toolbar.subtitle = getString(R.string.planning_title)
    }

}
