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
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListsBinding
import ke.co.definition.inkopies.databinding.ContentShoppingListsBinding
import ke.co.definition.inkopies.databinding.ItemShoppingListsBinding
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.list.ShoppingListActivity

class ShoppingListsActivity : AppCompatActivity() {

    private val vmObservables: MutableList<LiveData<Any>> = mutableListOf()
    private lateinit var viewAdapter: ShoppingListsAdapter
    private lateinit var viewModel: ShoppingListsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val views: ActivityShoppingListsBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_shopping_lists)

        val vmFactory = (application as App).appComponent.provideShoppingListsVMFactory()
        viewModel = ViewModelProviders.of(this, vmFactory)
                .get(ShoppingListsViewModel::class.java)
        views.vm = viewModel

        setSupportActionBar(views.toolbar)
        supportActionBar!!.setTitle(R.string.shopping_lists_title)
        prepRecyclerView(views.content)

        observeViewModel(views)
        observeViews(views)

    }

    override fun onResume() {
        super.onResume()
        viewModel.nextPage()
    }

    override fun onDestroy() {
        vmObservables.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    private fun observeViews(vs: ActivityShoppingListsBinding) {
        vs.fab.setOnClickListener { showNewShoppingListDialog() }
        viewAdapter.setOnItemSelectedListener { ShoppingListActivity.start(this, it) }
    }

    private fun observeViewModel(vs: ActivityShoppingListsBinding) {
        viewModel.nextPage.observe(this, Observer {
            viewAdapter.addShoppingLists(it ?: return@Observer)
        })
        viewModel.addedItem.observe(this, Observer {
            viewAdapter.add(it ?: return@Observer)
        })
        viewModel.snackbarData.observe(this, Observer { it?.show(vs.root) })

        @Suppress("UNCHECKED_CAST")
        vmObservables.addAll(mutableListOf(
                viewModel.nextPage as LiveData<Any>,
                viewModel.addedItem as LiveData<Any>,
                viewModel.snackbarData as LiveData<Any>,
                viewModel.progressNextPage as LiveData<Any>
        ))
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
            viewModel.onItemAdded(it ?: return@start)
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

    private var shoppingLists: MutableList<VMShoppingList> = mutableListOf()
    private var onItemSelectedListener: (it: VMShoppingList) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemShoppingListsHolder {

        val infl = LayoutInflater.from(parent.context)
        val views: ItemShoppingListsBinding = DataBindingUtil.inflate(infl,
                R.layout.item_shopping_lists, parent, false)
        return ItemShoppingListsHolder(views)
    }

    override fun getItemCount() = shoppingLists.size

    override fun onBindViewHolder(holder: ItemShoppingListsHolder, position: Int) {
        holder.binding.shoppingList = shoppingLists[position]
        holder.binding.layoutRoot.setOnClickListener {
            onItemSelectedListener(shoppingLists[position])
        }
    }

    fun setOnItemSelectedListener(l: (it: VMShoppingList) -> Unit) {
        onItemSelectedListener = l
    }

    fun addShoppingLists(shoppingLists: MutableList<VMShoppingList>) {
        val origiSize = this.shoppingLists.size
        this.shoppingLists.addAll(shoppingLists)
        notifyItemRangeInserted(origiSize, shoppingLists.size)
    }

    fun add(shoppingList: VMShoppingList) {
        shoppingLists.add(shoppingList)
        notifyItemInserted(shoppingLists.size - 1)
    }

    data class ItemShoppingListsHolder(internal val binding: ItemShoppingListsBinding) :
            RecyclerView.ViewHolder(binding.root)

}
