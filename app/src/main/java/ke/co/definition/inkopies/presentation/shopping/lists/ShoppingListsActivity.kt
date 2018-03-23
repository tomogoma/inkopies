package ke.co.definition.inkopies.presentation.shopping.lists

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListsBinding
import ke.co.definition.inkopies.databinding.ContentShoppingListsBinding
import ke.co.definition.inkopies.databinding.ItemShoppingListsBinding
import ke.co.definition.inkopies.model.shopping.ShoppingList

class ShoppingListsActivity : AppCompatActivity() {


    private lateinit var viewAdapter: ShoppingListsAdapter
    private val vmObservables: MutableList<LiveData<Any>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val views: ActivityShoppingListsBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_shopping_lists)

        val viewModel = ViewModelProviders.of(this).get(ShoppingListsViewModel::class.java)
        views.vm = viewModel

        setSupportActionBar(views.toolbar)
        views.toolbar.setSubtitle(R.string.shopping_lists_title)
        prepRecyclerView(views.content!!)

        observeViewModel(viewModel)
        observeViews(views)

        viewModel.start()
    }

    override fun onDestroy() {
        vmObservables.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    private fun observeViews(vs: ActivityShoppingListsBinding) {
        vs.fab.setOnClickListener { showNewShoppingListDialog() }
    }

    private fun observeViewModel(vm: ShoppingListsViewModel) {
        vm.shoppingLists.observe(this, Observer {
            viewAdapter.setShoppingLists(it ?: return@Observer)
        })

        @Suppress("UNCHECKED_CAST")
        vmObservables.add(vm.shoppingLists as LiveData<Any>)
    }

    private fun prepRecyclerView(vs: ContentShoppingListsBinding) {

        val viewManager = LinearLayoutManager(this)
        viewAdapter = ShoppingListsAdapter()

        vs.shoppingLists.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun showNewShoppingListDialog() {
        NewShoppingListDialogFrag.start(supportFragmentManager, {
            viewAdapter.add(it ?: return@start)
        })
    }

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, ShoppingListsActivity::class.java)
            activity.startActivity(intent)
        }
    }
}

class ShoppingListsAdapter :
        RecyclerView.Adapter<ShoppingListsAdapter.ItemShoppingListsHolder>() {

    private var shoppingLists: MutableList<ShoppingList> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemShoppingListsHolder {

        val infl = LayoutInflater.from(parent.context)
        val views: ItemShoppingListsBinding = DataBindingUtil.inflate(infl,
                R.layout.item_shopping_lists, parent, false)
        return ItemShoppingListsHolder(views)
    }

    override fun getItemCount() = shoppingLists.size

    override fun onBindViewHolder(holder: ItemShoppingListsHolder, position: Int) {
        holder.binding.shoppingList = shoppingLists[position]
    }

    fun setShoppingLists(shoppingLists: MutableList<ShoppingList>) {
        this.shoppingLists = shoppingLists
        notifyDataSetChanged()
    }

    fun add(shoppingList: ShoppingList) {
        shoppingLists.add(shoppingList)
        notifyItemChanged(shoppingLists.size - 1)
    }

    data class ItemShoppingListsHolder(internal val binding: ItemShoppingListsBinding) :
            RecyclerView.ViewHolder(binding.root)

}
