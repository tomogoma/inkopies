package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.Filterable
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogUpsertListItemBinding
import ke.co.definition.inkopies.presentation.common.SLMDialogFragment
import ke.co.definition.inkopies.presentation.common.hideKeyboard
import ke.co.definition.inkopies.presentation.common.onGlobalLayoutOnce
import ke.co.definition.inkopies.presentation.common.showKeyboard
import ke.co.definition.inkopies.presentation.shopping.common.SearchShoppingListItemResult
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import java.util.concurrent.atomic.AtomicLong


/**
 * Created by tomogoma
 * On 27/03/18.
 */
class UpsertListItemDialogFrag : SLMDialogFragment() {

    internal var onDismissCallback: (VMShoppingListItem?) -> Unit = {}
    private var rslt: VMShoppingListItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val views: DialogUpsertListItemBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_upsert_list_item, container, false)

        val vmFactory = (activity!!.application as App).appComponent.provideUpsertListItemVMFactory()
        val viewModel = ViewModelProviders.of(this, vmFactory)
                .get(UpsertListItemViewModel::class.java)
        views.vm = viewModel

        observeViewModel(viewModel, views)
        start(viewModel, views)
        setUpAutoCompletables(views, viewModel)
        observeViews(views, viewModel)

        return views.root
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onDismissCallback(rslt)
        super.onDismiss(dialog)
    }

    private fun setUpAutoCompletables(vs: DialogUpsertListItemBinding, vm: UpsertListItemViewModel) {
        setUpAutoComplete(vs.itemName, vm, vm::onSearchItemName, vm.searchItemNameResult)
        setUpAutoComplete(vs.brandName, vm, vm::onSearchBrandName, vm.searchBrandNameResult)
        setUpAutoComplete(vs.unitPrice, vm, vm::onSearchUnitPrice, vm.searchUnitPriceResult)
        setUpAutoComplete(vs.measuringUnit, vm, vm::onSearchMeasuringUnit,
                vm.searchMeasuringUnitResult)

        observedLiveData.addAll(listOf(vm.searchItemNameResult, vm.searchBrandNameResult,
                vm.searchUnitPriceResult, vm.searchMeasuringUnitResult))
    }

    private fun setUpAutoComplete(textView: AutoCompleteTextView, vm: UpsertListItemViewModel,
                                  search: (text: String) -> Unit,
                                  resultEvent: SingleLiveEvent<List<SearchShoppingListItemResult>>) {

        val adapter = AutoCompleteAdapter(this, search, resultEvent)
        textView.setAdapter(adapter)
        textView.setOnItemClickListener { _, _, pos, _ ->
            val rslt = adapter.getResultItem(pos)
            if (rslt.sli != null) {
                vm.onChangeShoppingListItem(rslt.sli)
            }
            textView.setText(rslt.printName, false)
        }
    }

    private fun observeViews(vs: DialogUpsertListItemBinding, vm: UpsertListItemViewModel) {
        vs.delete.setOnClickListener { vm.onDelete() }
        vs.submit.setOnClickListener { vm.onSubmit() }
        vs.unitPrice.setOnEditorActionListener { _, actionID, _ ->
            if (actionID != EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener false
            }
            vs.unitPrice.hideKeyboard(activity!!)
            vm.onSubmit()
            return@setOnEditorActionListener true
        }
    }

    private fun observeViewModel(vm: UpsertListItemViewModel, vs: DialogUpsertListItemBinding) {
        vm.snackBarData.observe(this, Observer { it?.show(vs.layoutRoot) })
        vm.finished.observe(this, Observer { rslt = it; dialog.dismiss() })
        observedLiveData.addAll(mutableListOf(vm.snackBarData, vm.finished))
    }

    private fun start(vm: UpsertListItemViewModel, vs: DialogUpsertListItemBinding) {

        val listStr = arguments!!.getString(EXTRA_LIST)
        val list = Gson().fromJson(listStr, VMShoppingList::class.java)

        val itemStr = arguments!!.getString(EXTRA_LIST_ITEM)
        if (itemStr == null || itemStr == "") {
            vm.start(list, null)
        } else {
            val item = Gson().fromJson(itemStr, VMShoppingListItem::class.java)
            vm.start(list, item)
        }

        vs.layoutRoot.onGlobalLayoutOnce {

            val focusStr = arguments!!.getString(EXTRA_FOCUS)
            val focus = ItemFocus.valueOf(focusStr)

            when (focus) {
                ItemFocus.BRAND -> vs.brandName.showKeyboard(activity!!)
                ItemFocus.ITEM -> vs.itemName.showKeyboard(activity!!)
                ItemFocus.MEASUREMENT_UNIT -> vs.measuringUnit.showKeyboard(activity!!)
                ItemFocus.UNIT_PRICE -> vs.unitPrice.showKeyboard(activity!!)
                ItemFocus.QUANTITY -> vs.quantity.showKeyboard(activity!!)
                ItemFocus.NONE -> {
                    /* no-op */
                }
            }
        }

    }

    companion object {
        private const val EXTRA_LIST_ITEM = "EXTRA_LIST_ITEM"
        private const val EXTRA_FOCUS = "EXTRA_FOCUS"
        private const val EXTRA_LIST = "EXTRA_LIST"

        fun start(fm: FragmentManager, list: VMShoppingList, item: VMShoppingListItem?,
                  focus: ItemFocus?, onDismissCallback: (vl: VMShoppingListItem?) -> Unit = {}) {
            UpsertListItemDialogFrag().apply {

                this.onDismissCallback = onDismissCallback

                arguments = Bundle().apply {
                    if (item != null) {
                        putString(EXTRA_LIST_ITEM, Gson().toJson(item))
                    }
                    putString(EXTRA_FOCUS, focus?.name ?: ItemFocus.BRAND.name)
                    putString(EXTRA_LIST, Gson().toJson(list))
                }

                show(fm, UpsertListItemDialogFrag::class.java.name)
            }
        }
    }

    class AutoCompleteAdapter(
            frag: UpsertListItemDialogFrag,
            private val search: (text: String) -> Unit,
            resultEvent: SingleLiveEvent<List<SearchShoppingListItemResult>>
    ) : ArrayAdapter<String>(frag.context, android.R.layout.simple_spinner_dropdown_item), Filterable {

        private val resultItems = mutableListOf<Pair<SearchShoppingListItemResult, Long>>()
        private var lastID = AtomicLong(0)

        init {
            resultEvent.observe(frag, Observer {
                synchronized(this@AutoCompleteAdapter) {
                    resultItems.clear()
                    it?.forEach { resultItems.add(Pair(it, lastID.addAndGet(1))) }
                    notifyDataSetChanged()
                }
            })
        }

        override fun getItem(pos: Int): String = resultItems[pos].first.printName

        override fun getItemId(pos: Int): Long = resultItems[pos].second

        override fun getCount(): Int = resultItems.size

        override fun getFilter() = object : Filter() {
            override fun performFiltering(searchTxt: CharSequence?): FilterResults {
                val results = FilterResults()
                search(searchTxt?.toString() ?: return results)
                return results
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                /* no-op*/
            }

        }

        fun getResultItem(pos: Int) = resultItems[pos].first

    }
}

