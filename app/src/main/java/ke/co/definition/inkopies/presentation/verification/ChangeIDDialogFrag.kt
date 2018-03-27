package ke.co.definition.inkopies.presentation.verification

import android.arch.lifecycle.LiveData
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
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogChangeIdentifierBinding
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.presentation.common.SLMDialogFragment

class ChangeIDDialogFrag : SLMDialogFragment() {

    private var onDismissCallback: (vl: VerifLogin?) -> Unit = {}
    private var result: VerifLogin? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val views = DataBindingUtil.inflate<DialogChangeIdentifierBinding>(inflater,
                R.layout.dialog_change_identifier, container, false)

        val uivmFactory = (activity!!.application as App).appComponent.updateIdentifierVMFactory()
        val viewModel = ViewModelProviders.of(this, uivmFactory)
                .get(UpdateIdentifierViewModel::class.java)
        views.vm = viewModel

        observeViews(views, viewModel)
        observeViewModel(viewModel, views)
        start(viewModel)

        return views.root
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onDismissCallback(result)
        super.onDismiss(dialog)
    }

    fun setOnDismissCallback(cb: (vl: VerifLogin?) -> Unit) {
        onDismissCallback = cb
    }

    private fun observeViewModel(vm: UpdateIdentifierViewModel, vs: DialogChangeIdentifierBinding) {

        vm.finishedEvent.observe(this, Observer {
            result = it
            dialog.dismiss()
        })
        vm.snackBarData.observe(this, Observer { it?.show(vs.layoutRoot) })

        @Suppress("UNCHECKED_CAST")
        observedLiveData.addAll(mutableListOf(
                vm.finishedEvent as LiveData<Any>,
                vm.snackBarData as LiveData<Any>
        ))
    }

    private fun observeViews(vs: DialogChangeIdentifierBinding, vm: UpdateIdentifierViewModel) {
        vs.identifier.setOnEditorActionListener({ _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                vm.onSubmit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        })
        vs.submit.setOnClickListener({ vm.onSubmit() })
        vs.cancel.setOnClickListener({ dialog.dismiss() })
    }

    private fun start(vm: UpdateIdentifierViewModel) {
        val identifier = arguments?.getString(EXTRA_WITH_IDENTIFIER) ?: return
        vm.setIdentifier(identifier)
    }

    companion object {

        val EXTRA_WITH_IDENTIFIER = ChangeIDDialogFrag::class.java.name + ".EXTRA_WITH_IDENTIFIER"

        fun start(fm: FragmentManager, onDismissCallback: (vl: VerifLogin?) -> Unit = {}) {
            start(fm, "", onDismissCallback)
        }

        fun start(fm: FragmentManager, identifier: String,
                  onDismissCallback: (vl: VerifLogin?) -> Unit = {}) {

            ChangeIDDialogFrag().apply {
                setOnDismissCallback(onDismissCallback)
                if (!identifier.isEmpty()) {
                    arguments = Bundle().apply { putString(EXTRA_WITH_IDENTIFIER, identifier) }
                }
                isCancelable = false
                show(fm, ChangeIDDialogFrag::class.java.name)
            }
        }
    }
}