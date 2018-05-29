package ke.co.definition.inkopies.presentation.shopping.list

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.databinding.ContentShoppingListBinding
import ke.co.definition.inkopies.databinding.ItemShoppingListBinding
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.presentation.common.InkopiesActivity
import ke.co.definition.inkopies.presentation.shopping.checkout.CheckoutDialogFrag
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem

class ShoppingListActivity : InkopiesActivity() {

    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var views: ActivityShoppingListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        views = DataBindingUtil.setContentView(this,
                R.layout.activity_shopping_list)

        val vmFactory = (application as App).appComponent.provideShoppingListVMFactory()
        viewModel = ViewModelProviders.of(this, vmFactory)
                .get(ShoppingListViewModel::class.java)
        views.vm = viewModel

        val viewAdapter = prepRecyclerView(views.content)

        observeViewModel(viewModel, views, viewAdapter)
        start(viewModel)
        observeViews(views, viewModel, viewAdapter)

        setSupportActionBar(views.toolbar)
        supportActionBar!!.title = viewModel.shoppingList.get()!!.name()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        viewModel.onCreateOptionsMenu()
        viewModel.menuRes.observe(this, Observer {
            menu?.clear()
            menuInflater.inflate(it!!, menu)
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.checkout -> onCheckout()
            R.id.export -> onExport()
            R.id.modePreparation -> viewModel.onChangeMode(ShoppingMode.PREPARATION)
            R.id.modeShopping -> viewModel.onChangeMode(ShoppingMode.SHOPPING)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun onCheckout() {
        CheckoutDialogFrag.start(supportFragmentManager, viewModel.shoppingList.get()!!.id)
    }

    private fun onExport() {
        if (haveWriteExtFilePerms()) {
            viewModel.onExport()
            return
        }
        if (shouldShowWriteExtFileRationale()) {
            Snackbar.make(views.root, R.string.allow_storage_access_to_export, Snackbar.LENGTH_LONG)
                    .show()
        }
        requestWriteExtFilePerm()
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

    private fun observeViews(vs: ActivityShoppingListBinding, vm: ShoppingListViewModel, va: ShoppingListAdapter) {
        vs.fab.setOnClickListener {
            UpsertListItemDialogFrag.start(supportFragmentManager, vm.shoppingList.get()!!, null, null,
                    { vm.onItemAdded(it ?: return@start) })
        }
        va.setOnItemSelectedListener(object : ActionListener {
            override fun onItemSelected(item: VMShoppingListItem, focus: ItemFocus) {
                UpsertListItemDialogFrag.start(supportFragmentManager, vm.shoppingList.get()!!, item, focus,
                        {
                            if (it != null) {
                                vm.onItemUpdated(item, it)
                            } else {
                                vm.onItemDeleted(item)
                            }
                        })
            }

            override fun onCheckChanged(item: VMShoppingListItem, newState: Boolean) {
                vm.onItemSelectionChanged(item, newState)
            }
        })
    }

    private fun observeViewModel(vm: ShoppingListViewModel, vs: ActivityShoppingListBinding, va: ShoppingListAdapter) {
        vm.snackbarData.observe(this, Observer { it?.show(vs.root) })
        vm.nextPage.observe(this, Observer { va.addItems(it ?: return@Observer) })
        vm.itemUpdate.observe(this, Observer {
            va.updateItem(it ?: return@Observer)
        })
        vm.newItem.observe(this, Observer { va.add(it ?: return@Observer) })
        vm.itemDelete.observe(this, Observer { va.removeItem(it ?: return@Observer) })
        vm.clearList.observe(this, Observer { if (it == true) va.clear() })

        observedLiveData.addAll(mutableListOf(vm.snackbarData, vm.nextPage, vm.itemUpdate,
                vm.newItem, vm.itemDelete, vm.clearList))
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
        fun onCheckChanged(item: VMShoppingListItem, newState: Boolean)
    }

    class ShoppingListAdapter :
            RecyclerView.Adapter<ShoppingListAdapter.ItemShoppingListHolder>() {

        private var listener: ActionListener = object : ActionListener {
            override fun onItemSelected(item: VMShoppingListItem, focus: ItemFocus) {
                /* No-op */
            }

            override fun onCheckChanged(item: VMShoppingListItem, newState: Boolean) {
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

        override fun onBindViewHolder(holder: ItemShoppingListHolder, bindPos: Int) {

            val item = items[bindPos]
            holder.binding.item = item

            holder.binding.brand.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.BRAND)
            }
            holder.binding.name.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.ITEM)
            }
            holder.binding.root.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.ITEM)
            }
            holder.binding.measuringUnit.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.MEASUREMENT_UNIT)
            }
            holder.binding.unitPrice.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.UNIT_PRICE)
            }
            holder.binding.x.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.QUANTITY)
            }
            holder.binding.quantity.setOnClickListener {
                listener.onItemSelected(item, ItemFocus.QUANTITY)
            }
            holder.binding.checked.setOnCheckedChangeListener { _, state ->
                if (item.isChecked() == state) return@setOnCheckedChangeListener
                listener.onCheckChanged(item, state)
            }
        }

        fun setOnItemSelectedListener(l: ActionListener) {
            listener = l
        }

        fun clear() {
            synchronized(this) {
                items.clear()
                notifyDataSetChanged()
            }
        }

        fun addItems(items: MutableList<VMShoppingListItem>) {
            synchronized(this) {
                val origiSize = this.items.size
                this.items.addAll(items)
                notifyItemRangeInserted(origiSize, items.size)
            }
        }

        fun updateItem(newVal: VMShoppingListItem) {
            synchronized(this) {
                val pos = items.indexOfFirst { it.sli.id == newVal.sli.id }
                if (pos >= 0) {
                    this.items.removeAt(pos)
                }
                val newPos = calculateNewPos(newVal)
                this.items.add(newPos, newVal)
                when (pos) {
                    newPos -> notifyItemChanged(newPos)
                    -1 -> notifyItemInserted(newPos)
                    else -> {
                        notifyItemMoved(pos, newPos)
                        notifyItemChanged(newPos)
                    }
                }
            }
        }

        fun removeItem(item: VMShoppingListItem) {
            synchronized(this) {
                val pos = items.indexOfFirst { it.sli.id == item.sli.id }
                if (pos < 0) {
                    return@synchronized
                }
                this.items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }

        fun add(item: VMShoppingListItem) {
            synchronized(this) {
                val newPos = calculateNewPos(item)
                items.add(newPos, item)
                notifyItemInserted(newPos)
            }
        }

        private fun calculateNewPos(item: VMShoppingListItem): Int {
            synchronized(this) {

                if (items.size == 0) {
                    return 0
                }

                // We know checked items come first, followed by unchecked items.
                var firstUncheckedPos = items.indexOfFirst { !it.isChecked() }
                if (firstUncheckedPos == -1) {
                    firstUncheckedPos = items.size
                }

                return if (item.isChecked()) {
                    calculateNewPos(item, 0, firstUncheckedPos)
                } else {
                    calculateNewPos(item, firstUncheckedPos, items.size)
                }
            }
        }

        /**
         * calculateNewPos calculates the position of item in the items list limited to the
         * range defined by offsetPos (inclusive) and lastPos (exclusive). Returns 0 if the list
         * is empty or lastPos is 0.
         */
        private fun calculateNewPos(item: VMShoppingListItem, offsetPos: Int, lastPos: Int): Int {
            synchronized(this) {

                if (items.size == 0 || lastPos == 0) {
                    return 0
                }

                var frstSameCatPos = 0
                var lstSameCatPos = 0

                val orderPos = items
                        .subList(offsetPos, lastPos)
                        .apply {

                            frstSameCatPos = indexOfFirst { it.categoryName() == item.categoryName() }
                            if (frstSameCatPos > -1) {
                                lstSameCatPos = indexOfLast { it.categoryName() == item.categoryName() } + 1
                                return@apply
                            }

                            frstSameCatPos = indexOfFirst { it.categoryName() > item.categoryName() }
                            if (frstSameCatPos == -1) frstSameCatPos = size
                            lstSameCatPos = frstSameCatPos
                        }
                        .subList(frstSameCatPos, lstSameCatPos)
                        .indexOfFirst { it.itemName() >= item.itemName() }
                return if (orderPos == -1) {
                    offsetPos + lstSameCatPos
                } else {
                    offsetPos + frstSameCatPos + orderPos
                }
            }
        }

        data class ItemShoppingListHolder(internal val binding: ItemShoppingListBinding) :
                RecyclerView.ViewHolder(binding.root)

    }

}
