package ke.co.definition.inkopies.presentation.shopping.list

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListBinding
import ke.co.definition.inkopies.databinding.ContentShoppingListBinding
import ke.co.definition.inkopies.databinding.ItemShoppingListBinding
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.presentation.common.InkopiesActivity
import ke.co.definition.inkopies.presentation.shopping.checkout.CheckoutDialogFrag
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem

class ShoppingListActivity : InkopiesActivity() {

    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var views: ActivityShoppingListBinding
    private lateinit var shoppingListObs: Observable.OnPropertyChangedCallback
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        views = DataBindingUtil.setContentView(this,
                R.layout.activity_shopping_list)

        val vmFactory = (application as App).appComponent.provideShoppingListVMFactory()
        viewModel = ViewModelProviders.of(this, vmFactory)
                .get(ShoppingListViewModel::class.java)
        views.vm = viewModel

        val viewAdapter = prepRecyclerView(views.content)

        setSupportActionBar(views.toolbar)

        observeViewModel(viewModel, views, viewAdapter)
        start(viewModel)
        observeViews(views, viewModel, viewAdapter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu

        viewModel.menuRes.observe(this, Observer {
            menu?.clear()
            menuInflater.inflate(it!!, menu)
        })
        observedLiveData.add(viewModel.menuRes)

        viewModel.onCreateOptionsMenu()
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

    override fun onDestroy() {
        viewModel.shoppingList.removeOnPropertyChangedCallback(shoppingListObs)
        super.onDestroy()
    }

    private fun onCheckout() {
        val listID = viewModel.shoppingList.get()?.id ?: return
        CheckoutDialogFrag.start(supportFragmentManager, listID)
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
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        return viewAdapter
    }

    private fun observeViews(vs: ActivityShoppingListBinding, vm: ShoppingListViewModel, va: ShoppingListAdapter) {
        vs.fab.setOnClickListener {
            val list = vm.shoppingList.get() ?: return@setOnClickListener
            UpsertListItemDialogFrag.start(supportFragmentManager, list, null, null)
        }
        va.setOnItemSelectedListener(object : ActionListener {
            override fun onItemSelected(item: VMShoppingListItem, focus: ItemFocus) {
                val list = vm.shoppingList.get() ?: return
                item.isUpdating.set(true)
                UpsertListItemDialogFrag.start(supportFragmentManager, list, item, focus,
                        { item.isUpdating.set(false) })
            }

            override fun onCheckChanged(item: VMShoppingListItem, newState: Boolean) {
                vm.onItemSelectionChanged(item, newState)
            }
        })
    }

    private fun observeViewModel(vm: ShoppingListViewModel, vs: ActivityShoppingListBinding,
                                 va: ShoppingListAdapter) {

        vm.snackbarData.observe(this, Observer { it?.show(vs.root) })
        vm.items.observe(this, Observer {
            va.setItems(it ?: return@Observer)
            menu?.findItem(R.id.checkout)?.isVisible = va.hasCartedItem()
        })
        observedLiveData.addAll(mutableListOf(vm.snackbarData, vm.items))

        shoppingListObs = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                supportActionBar!!.title = viewModel.shoppingList.get()!!.name()
            }
        }
        vm.shoppingList.addOnPropertyChangedCallback(shoppingListObs)
    }

    private fun start(vm: ShoppingListViewModel) {
        val slID = intent.getStringExtra(EXTRA_SHOPPING_LIST_ID)
        vm.start(slID)
    }

    companion object {

        private const val EXTRA_SHOPPING_LIST_ID = "EXTRA_SHOPPING_LIST_ID"

        fun start(activity: Activity, shoppingListID: String) {
            val i = Intent(activity, ShoppingListActivity::class.java)
            i.putExtra(EXTRA_SHOPPING_LIST_ID, shoppingListID)
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

        fun setItems(changes: Pair<DiffUtil.DiffResult?, MutableList<VMShoppingListItem>>) {
            items.clear()
            items = changes.second
            changes.first?.dispatchUpdatesTo(this) ?: notifyDataSetChanged()
        }

        fun hasCartedItem(): Boolean {
            return items.indexOfFirst { it.inCart } != -1
        }

        data class ItemShoppingListHolder(internal val binding: ItemShoppingListBinding) :
                RecyclerView.ViewHolder(binding.root)

    }

}
