package ke.co.definition.inkopies.presentation.verification

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityVerifyBinding
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
        observeViewModel()
        observeViews(views)
        if (savedInstanceState == null) start()
    }

    override fun onDestroy() {
        liveDataObservations.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    private fun observeViewModel() {

        viewModel.openEditDialog.observe(this, Observer {
            TODO("Implement edit identifier dialog")
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
        views.identifier.setOnClickListener({ TODO() })
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

    companion object {

        private val EXTRA_VERIF_LOGIN = VerifyActivity::class.java.name + "EXTRA_VERIF_LOGIN"

        fun startForResult(a: AppCompatActivity, vl: VerifLogin, reqCode: Int) {
            val i = Intent(a, VerifyActivity::class.java).apply {
                putExtra(EXTRA_VERIF_LOGIN, Gson().toJson(vl))
            }
            a.startActivityForResult(i, reqCode)
        }
    }
}
