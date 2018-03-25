package ke.co.definition.inkopies.presentation.shopping.list

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.model.shopping.ShoppingList

class ShoppingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val views: ActivityShoppingListBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_shopping_list)

        val vmFactory = (application as App).appComponent.provideShoppingListVMFactory()
        val viewModel = ViewModelProviders.of(this, vmFactory)
                .get(ShoppingListViewModel::class.java)
        views.vm = viewModel

        setSupportActionBar(views.toolbar)

        observeViewModel(viewModel)
        observeViews(views)

        start(viewModel)
    }

    private fun observeViews(views: ActivityShoppingListBinding) {
        views.fab.setOnClickListener { }
    }

    private fun observeViewModel(vm: ShoppingListViewModel) {

    }

    private fun start(vm: ShoppingListViewModel) {
        val slStr = intent.getStringExtra(EXTRA_SHOPPING_LIST)
        val sl = Gson().fromJson(slStr, ShoppingList::class.java)
        vm.start(sl)
    }

    companion object {

        private const val EXTRA_SHOPPING_LIST = "EXTRA_SHOPPING_LIST"

        fun start(activity: Activity, shoppingList: ShoppingList) {
            val i = Intent(activity, ShoppingListActivity::class.java)
            i.putExtra(EXTRA_SHOPPING_LIST, Gson().toJson(shoppingList))
            activity.startActivity(i)
        }
    }

}
