package ke.co.definition.inkopies.presentation.shopping.lists

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogNewShoppingListBinding
import ke.co.definition.inkopies.presentation.common.SLMDialogFragment
import ke.co.definition.inkopies.presentation.shopping.common.VMShoppingList

/**
 * Created by tomogoma
 * On 22/03/18.
 */
class NewShoppingListDialogFrag : SLMDialogFragment() {

    private var shoppingList: VMShoppingList? = null
    private var onDismissCallback: (shoppingList: VMShoppingList?) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val views: DialogNewShoppingListBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_new_shopping_list, container, false)

        val vmFactory = (activity!!.application as App).appComponent.provideNewShoppingListVMFactory()
        val vm = ViewModelProviders.of(this, vmFactory).get(NewShoppingListViewModel::class.java)
        views.vm = vm

        observeViewModel(vm, views)
        observeViews(views, vm)

        return views.root
    }

    private fun observeViewModel(vm: NewShoppingListViewModel, vs: DialogNewShoppingListBinding) {
        vm.snackbarData.observe(this, Observer { it?.show(vs.layoutRoot) })
        vm.finished.observe(this, Observer { shoppingList = it; dismiss() })

        @Suppress("UNCHECKED_CAST")
        observedLiveData.addAll(mutableListOf(
                vm.snackbarData as LiveData<Any>,
                vm.finished as LiveData<Any>
        ))
    }

    private fun observeViews(vs: DialogNewShoppingListBinding, vm: NewShoppingListViewModel) {
        vs.cancel.setOnClickListener { dialog.dismiss() }
        vs.submit.setOnClickListener { vm.onCreateShoppingList() }
        vs.name.setOnEditorActionListener({ _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                vm.onCreateShoppingList()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        })
    }

    override fun dismiss() {
        onDismissCallback(shoppingList)
        super.dismiss()
    }

    companion object {

        fun start(fm: FragmentManager, onDismissCallback: (vl: VMShoppingList?) -> Unit = {}) {
            NewShoppingListDialogFrag().apply {
                this.onDismissCallback = onDismissCallback
                show(fm, NewShoppingListDialogFrag::class.java.name)
            }
        }
    }
}