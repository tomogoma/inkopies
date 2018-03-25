package ke.co.definition.inkopies.presentation.shopping.list

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList

class ShoppingListActivity : AppCompatActivity() {

    private val liveDataObservables: MutableList<LiveData<Any>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val views: ActivityShoppingListBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_shopping_list)

        val vmFactory = (application as App).appComponent.provideShoppingListVMFactory()
        val viewModel = ViewModelProviders.of(this, vmFactory)
                .get(ShoppingListViewModel::class.java)
        views.vm = viewModel

        setSupportActionBar(views.toolbar)

        observeViewModel(viewModel, views)
        observeViews(views)

        start(viewModel)
    }

    override fun onDestroy() {
        liveDataObservables.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    private fun observeViews(views: ActivityShoppingListBinding) {
        views.fab.setOnClickListener { }
    }

    private fun observeViewModel(vm: ShoppingListViewModel, vs: ActivityShoppingListBinding) {
        vm.snackbarData.observe(this, Observer { it?.show(vs.root) })
        vm.nextPage.observe(this, Observer { })
    }

    private fun start(vm: ShoppingListViewModel) {
        val slStr = intent.getStringExtra(EXTRA_SHOPPING_LIST)
        val sl = Gson().fromJson(slStr, VMShoppingList::class.java)
        vm.start(sl)
    }

    companion object {

        private const val EXTRA_SHOPPING_LIST = "EXTRA_SHOPPING_LIST"

        fun start(activity: Activity, shoppingList: VMShoppingList) {
            val i = Intent(activity, ShoppingListActivity::class.java)
            i.putExtra(EXTRA_SHOPPING_LIST, Gson().toJson(shoppingList))
            activity.startActivity(i)
        }
    }

}
