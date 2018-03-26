package ke.co.definition.inkopies.presentation.shopping.list

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
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.databinding.ContentShoppingListBinding
import ke.co.definition.inkopies.databinding.ItemShoppingListBinding
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem

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

        val viewAdapter = prepRecyclerView(views.content!!)
        setSupportActionBar(views.toolbar)

        observeViewModel(viewModel, views, viewAdapter)
        observeViews(views, viewModel, viewAdapter)

        start(viewModel)
    }

    override fun onDestroy() {
        liveDataObservables.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    private fun prepRecyclerView(vs: ContentShoppingListBinding): ShoppingListAdapter {

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = ShoppingListAdapter()

        vs.shoppingLists.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return viewAdapter
    }

    private fun observeViews(views: ActivityShoppingListBinding, vm: ShoppingListViewModel, va: ShoppingListAdapter) {
        views.fab.setOnClickListener { TODO("Show new shopping list item") }
        va.setOnItemSelectedListener(object : ActionListener {
            override fun onItemSelected(item: VMShoppingListItem, focus: ItemFocus) {
                TODO("Show edit item dialog")
            }

            override fun onCheckChanged(item: VMShoppingListItem, pos: Int, newState: Boolean) {
                vm.onItemSelectionChanged(item, pos, newState)
            }
        })
    }

    private fun observeViewModel(vm: ShoppingListViewModel, vs: ActivityShoppingListBinding, va: ShoppingListAdapter) {
        vm.snackbarData.observe(this, Observer { it?.show(vs.root) })
        vm.nextPage.observe(this, Observer { va.addItems(it ?: return@Observer) })
        vm.itemUpdate.observe(this, Observer {
            va.updateItem(it?.first ?: return@Observer, it.second)
        })

        @Suppress("UNCHECKED_CAST")
        liveDataObservables.addAll(mutableListOf(
                vm.snackbarData as LiveData<Any>,
                vm.nextPage as LiveData<Any>,
                vm.itemUpdate as LiveData<Any>
        ))
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

    interface ActionListener {
        fun onItemSelected(item: VMShoppingListItem, focus: ItemFocus)
        fun onCheckChanged(item: VMShoppingListItem, pos: Int, newState: Boolean)
    }

    class ShoppingListAdapter :
            RecyclerView.Adapter<ShoppingListAdapter.ItemShoppingListHolder>() {

        private var listener: ActionListener = object : ActionListener {
            override fun onItemSelected(item: VMShoppingListItem, focus: ItemFocus) {
                /* No-op */
            }

            override fun onCheckChanged(item: VMShoppingListItem, pos: Int, newState: Boolean) {
                /* No-op */
            }
        }

        private var items: MutableList<VMShoppingListItem> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemShoppingListHolder {

            val infl = LayoutInflater.from(parent.context)
            val views: ItemShoppingListBinding = DataBindingUtil.inflate(infl,
                    R.layout.item_shopping_list, parent, false)
            return ItemShoppingListHolder(views)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ItemShoppingListHolder, position: Int) {

            val item = items[position]
            holder.binding.item = item

            holder.binding.brand.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.BRAND)
            }
            holder.binding.name.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.ITEM)
            }
            holder.binding.measuringUnit.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.MEASUREMENT_UNIT)
            }
            holder.binding.unitPrice.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.UNIT_PRICE)
            }
            holder.binding.quantity.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.QUANTITY)
            }
            holder.binding.checked.setOnCheckedChangeListener { _, state ->
                if (item.isChecked() == state) return@setOnCheckedChangeListener
                listener.onCheckChanged(item, position, state)
            }
        }

        fun setOnItemSelectedListener(l: ActionListener) {
            listener = l
        }

        fun addItems(items: MutableList<VMShoppingListItem>) {
            val origiSize = this.items.size
            this.items.addAll(items)
            notifyItemRangeInserted(origiSize, items.size)
        }

        fun updateItem(newVal: VMShoppingListItem, pos: Int) {
            this.items.removeAt(pos)
            this.items.add(pos, newVal)
            notifyItemChanged(pos)
        }

        fun add(item: VMShoppingListItem) {
            items.add(item)
            notifyItemInserted(items.size - 1)
        }

        data class ItemShoppingListHolder(internal val binding: ItemShoppingListBinding) :
                RecyclerView.ViewHolder(binding.root)

    }

}
