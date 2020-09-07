package ke.co.definition.inkopies.presentation.shopping.lists

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityShoppingListsBinding
import ke.co.definition.inkopies.databinding.ContentShoppingListsBinding
import ke.co.definition.inkopies.databinding.ItemShoppingListsBinding
import ke.co.definition.inkopies.presentation.common.InkopiesActivity
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.list.ShoppingListActivity


class ShoppingListsActivity : InkopiesActivity() {

    private lateinit var viewAdapter: ShoppingListsAdapter
    private lateinit var viewModel: ShoppingListsViewModel
    private lateinit var views: ActivityShoppingListsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        views = DataBindingUtil.setContentView(this,
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

        viewModel.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showProfile -> showProfile()
            R.id.logout -> logout()
            R.id.export -> onExport()
            R.id.importLists -> onImport()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERM_REQ_WRITE_EXT_STORAGE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    return // permission not granted
                }
                onExport()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                viewModel.onImport(data.data!!)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    fun onImport() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    private fun onExport() {
        if (haveWriteExtFilePerms()) {
            viewModel.onExport()
            return
        }
        if (shouldShowWriteExtFileRationale()) {
            Snackbar.make(views.rootLayout, R.string.allow_storage_access_to_export, Snackbar.LENGTH_LONG)
                    .show()
        }
        requestWriteExtFilePerm()
    }

    private fun observeViews(vs: ActivityShoppingListsBinding) {
        vs.fab.setOnClickListener {
            NewShoppingListDialogFrag.start(supportFragmentManager, onDismissCallback = {})
        }
        viewAdapter.setOnItemSelectedListener { ShoppingListActivity.start(this, it.id) }
    }

    private fun observeViewModel(vs: ActivityShoppingListsBinding) {
        viewModel.shoppingLists.observe(this, Observer {
            viewAdapter.setShoppingLists(it ?: return@Observer)
        })
        viewModel.snackbarData.observe(this, Observer { it?.show(vs.root) })

        observedLiveData.addAll(mutableListOf(viewModel.shoppingLists, viewModel.snackbarData,
                viewModel.progressNextPage))
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

    companion object {

        private const val READ_REQUEST_CODE = 2

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

    fun setShoppingLists(shoppingLists: MutableList<VMShoppingList>) {
        this.shoppingLists = shoppingLists
        notifyDataSetChanged()
    }

    fun add(shoppingList: VMShoppingList) {
        shoppingLists.add(shoppingList)
        notifyItemInserted(shoppingLists.size - 1)
    }

    data class ItemShoppingListsHolder(internal val binding: ItemShoppingListsBinding) :
            RecyclerView.ViewHolder(binding.root)

}
