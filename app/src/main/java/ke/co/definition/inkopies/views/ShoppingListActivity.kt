package ke.co.definition.inkopies.views

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import java.util.*

class ShoppingListActivity : AppCompatActivity() {

    companion object {
        val EXTRA_SHOPPING_LIST_ID = ShoppingListActivity::class.java.name + "EXTRA_SHOPPING_LIST_ID"
        fun start(a: Activity, shoppingLIstID: UUID) {
            val i = Intent(a, ShoppingListActivity::class.java)
            i.putExtra(EXTRA_SHOPPING_LIST_ID, shoppingLIstID)
            a.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityShoppingListBinding>(this, R.layout.activity_shopping_list)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val shoppingLIstID: UUID = intent.getSerializableExtra(EXTRA_SHOPPING_LIST_ID) as UUID
        supportFragmentManager.beginTransaction()
                .add(R.id.frame, ShoppingListPlanFragment.initialize(shoppingLIstID))
                .commit()
    }

}
