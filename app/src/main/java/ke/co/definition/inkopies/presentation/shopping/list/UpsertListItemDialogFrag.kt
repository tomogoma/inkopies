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
        start(viewModel)
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
            vm.onSubmit()
            return@setOnEditorActionListener true
        }
    }

    private fun observeViewModel(vm: UpsertListItemViewModel, vs: DialogUpsertListItemBinding) {
    }

    private fun start(vm: UpsertListItemViewModel) {

        val itemStr = arguments?.getString(EXTRA_LIST_ITEM)
        if (itemStr == null || itemStr == "") {
            vm.start(null)
            return
        }

        val item = Gson().fromJson(itemStr, VMShoppingListItem::class.java)
        vm.start(item)
    }

    companion object {
        private const val EXTRA_LIST_ITEM = "EXTRA_LIST_ITEM"

        fun start(fm: FragmentManager, item: VMShoppingListItem?,
                  onDismissCallback: (vl: VMShoppingListItem?) -> Unit = {}) {
            UpsertListItemDialogFrag().apply {
                this.onDismissCallback = onDismissCallback
                if (item != null) {
                    arguments = Bundle().apply { putString(EXTRA_LIST_ITEM, Gson().toJson(item)) }
                }
                show(fm, UpsertListItemDialogFrag::class.java.name)
            }
        }
    }
}