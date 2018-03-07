package ke.co.definition.inkopies.presentation.verification

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityVerifyBinding
import ke.co.definition.inkopies.databinding.ChangeIdentifierDialogBinding
import ke.co.definition.inkopies.model.auth.VerifLogin


class VerifyActivity : AppCompatActivity() {

    private lateinit var layoutRoot: View
    private lateinit var viewModel: VerificationViewModel
    private val liveDataObservations: MutableList<LiveData<Any>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val views = DataBindingUtil.setContentView<ActivityVerifyBinding>(this, R.layout.activity_verify)
        layoutRoot = views.layoutRoot

        val vvmFactory = (application as App).appComponent.verificationVMFactory()
        viewModel = ViewModelProviders.of(this, vvmFactory)
                .get(VerificationViewModel::class.java)
        views.vm = viewModel
        observeViews(views)
        if (savedInstanceState == null) start()
    }

    override fun onDestroy() {
        stopObservingViewModel()
        super.onDestroy()
    }

    private fun observeViewModel() {

        viewModel.openEditDialog.observe(this, Observer {
            if (it == true) openChangeIdentifierDialog()
        })

        viewModel.finishedEv.observe(this, Observer {
            setResult(Activity.RESULT_OK)
            finish()
        })

        viewModel.snackBarData.observe(this, Observer { it?.show(layoutRoot) })

        @Suppress("UNCHECKED_CAST")
        liveDataObservations.addAll(listOf(
                viewModel.openEditDialog as LiveData<Any>,
                viewModel.finishedEv as LiveData<Any>,
                viewModel.snackBarData as LiveData<Any>
        ))
    }

    private fun observeViews(views: ActivityVerifyBinding) {
        views.identifier.setOnClickListener({ openChangeIdentifierDialog() })
        views.otp.setOnEditorActionListener({ _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSubmit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        })
        views.submit.setOnClickListener({ viewModel.onSubmit() })
        views.resendLink.setOnClickListener({ viewModel.onRequestResendOTP() })
        views.alreadyVerifiedLink.setOnClickListener({ viewModel.onClaimVerified() })
    }

    private fun start() {
        val verifLoginStr = intent.getStringExtra(EXTRA_VERIF_LOGIN)
        val verifLogin = Gson().fromJson(verifLoginStr, VerifLogin::class.java)
        viewModel.start(verifLogin)
    }

    private fun openChangeIdentifierDialog() {
        stopObservingViewModel()
        ChangeIDDialogFrag().apply {
            setOnDismissCallback { observeViewModel() }
            show(supportFragmentManager, ChangeIDDialogFrag::class.java.name)
        }
    }

    private fun stopObservingViewModel() {
        liveDataObservations.forEach { it.removeObservers(this) }
    }

    companion object {

        private val EXTRA_VERIF_LOGIN = VerifyActivity::class.java.name + "EXTRA_VERIF_LOGIN"

        fun startForResult(a: AppCompatActivity, vl: VerifLogin, reqCode: Int) {
            val i = Intent(a, VerifyActivity::class.java).apply {
                putExtra(EXTRA_VERIF_LOGIN, Gson().toJson(vl))
            }
            a.startActivityForResult(i, reqCode)
        }
    }

    class ChangeIDDialogFrag : DialogFragment() {

        private val observedLiveData: MutableList<LiveData<Any>> = mutableListOf()
        private var onDismissCallback: () -> Unit = {}

        override fun onCreateView(i: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val views = DataBindingUtil.inflate<ChangeIdentifierDialogBinding>(i,
                    R.layout.change_identifier_dialog, container, false)

            val vvmFactory = (activity.application as App).appComponent.verificationVMFactory()
            val viewModel = ViewModelProviders.of(activity, vvmFactory)
                    .get(VerificationViewModel::class.java)
            views.vm = viewModel

            observeViews(views)
            observeViewModel(viewModel, views)

            return views.root
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            return dialog
        }

        override fun onDestroy() {
            observedLiveData.forEach { it.removeObservers(this) }
            super.onDestroy()
        }

        override fun onDismiss(dialog: DialogInterface?) {
            onDismissCallback()
            super.onDismiss(dialog)
        }

        fun setOnDismissCallback(cb: () -> Unit) {
            onDismissCallback = cb
        }

        private fun observeViewModel(vm: VerificationViewModel, vs: ChangeIdentifierDialogBinding) {

            vm.finishedChangeIdentifierEv.observe(this, Observer { dialog.dismiss() })
            vm.snackBarData.observe(this, Observer { it?.show(vs.layoutRoot) })

            @Suppress("UNCHECKED_CAST")
            observedLiveData.addAll(mutableListOf(
                    vm.finishedChangeIdentifierEv as LiveData<Any>,
                    vm.snackBarData as LiveData<Any>
            ))
        }

        private fun observeViews(vs: ChangeIdentifierDialogBinding) {
            vs.identifier.setOnEditorActionListener({ _, actionID, _ ->
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    vs.vm!!.onSubmitChangeIdentifier()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            })
            vs.submit.setOnClickListener({ vs.vm!!.onSubmitChangeIdentifier() })
            vs.cancel.setOnClickListener({ dialog.dismiss() })
        }
    }
}
