package ke.co.definition.inkopies.presentation.shopping.list

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogUpsertListItemBinding
import ke.co.definition.inkopies.presentation.common.SLMDialogFragment
import ke.co.definition.inkopies.presentation.common.hideKeyboard
import ke.co.definition.inkopies.presentation.common.onGlobalLayoutOnce
import ke.co.definition.inkopies.presentation.common.showKeyboard
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingListItem


/**
 * Created by tomogoma
 * On 27/03/18.
 */
class UpsertListItemDialogFrag : SLMDialogFragment() {

    internal var onDismissCallback: (VMShoppingListItem?) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val views: DialogUpsertListItemBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_upsert_list_item, container, false)

        val vmFactory = (activity!!.application as App).appComponent.provideUpsertListItemVMFactory()
        val viewModel = ViewModelProviders.of(this, vmFactory)
                .get(UpsertListItemViewModel::class.java)
        views.vm = viewModel

        observeViewModel(viewModel, views)
        start(viewModel, views)
        observeViews(views, viewModel)

        return views.root
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
    }

    private fun start(vm: UpsertListItemViewModel, vs: DialogUpsertListItemBinding) {

        val itemStr = arguments!!.getString(EXTRA_LIST_ITEM)
        if (itemStr == null || itemStr == "") {
            vm.start(null)
        } else {
            val item = Gson().fromJson(itemStr, VMShoppingListItem::class.java)
            vm.start(item)
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

        fun start(fm: FragmentManager, item: VMShoppingListItem?, focus: ItemFocus?,
                  onDismissCallback: (vl: VMShoppingListItem?) -> Unit = {}) {
            UpsertListItemDialogFrag().apply {

                this.onDismissCallback = onDismissCallback

                arguments = Bundle().apply {
                    if (item != null) {
                        putString(EXTRA_LIST_ITEM, Gson().toJson(item))
                    }
                    putString(EXTRA_FOCUS, focus?.name ?: ItemFocus.NONE.name)
                }

                show(fm, UpsertListItemDialogFrag::class.java.name)
            }
        }
    }
}

