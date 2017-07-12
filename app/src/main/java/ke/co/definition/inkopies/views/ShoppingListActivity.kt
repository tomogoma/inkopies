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
        binding.title.text = sl.name
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
            ShoppingList.Mode.SHOPPING -> menuModeShopping()
            else -> menuModePlanning()
        }
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

    override fun setPrice(total_selected: Pair<Float, Float>) {
        val totalPriceStr = getString(R.string.total_price_title_fmt)
        binding.toolbarStartText.text = String.format(totalPriceStr, total_selected.second)
    }

    private fun initiateGoShopping() {
        sl.currMode = ShoppingList.Mode.SHOPPING
        Model.upsertShoppingList(sl)
        menuModeShopping()
        currFragment.loadList(sl)
    }

    private fun initiatePlanning() {
        sl.currMode = ShoppingList.Mode.PLANNING
        Model.upsertShoppingList(sl)
        menuModePlanning()
        currFragment.loadList(sl)
    }

    private fun menuModeShopping() {
        goShopping.isVisible = false
        checkout.isVisible = true
    }

    private fun menuModePlanning() {
        checkout.isVisible = false
        goShopping.isVisible = true
    }

}
