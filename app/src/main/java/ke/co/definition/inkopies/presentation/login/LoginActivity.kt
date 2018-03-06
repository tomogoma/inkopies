package ke.co.definition.inkopies.presentation.login

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityLoginBinding
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.presentation.common.replaceFrag
import ke.co.definition.inkopies.presentation.common.replaceFragBackStack
import ke.co.definition.inkopies.presentation.profile.ProfileActivity
import ke.co.definition.inkopies.presentation.verification.VerifyActivity

class LoginActivity : AppCompatActivity(), LoginFragCoordinator {

    private lateinit var loginVM: LoginViewModel
    private val liveDataObservations: MutableList<LiveData<Any>> = mutableListOf()

    // Only required if not logged in, so bind views lazily
    private val binding: ActivityLoginBinding by lazy {
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.vm = loginVM
        return@lazy binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lvmFactory = (application as App).appComponent.loginVMFactory()
        loginVM = ViewModelProviders.of(this, lvmFactory).get(LoginViewModel::class.java)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        loginVM.checkLoggedIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_VERIFY_REGISTRATION) {
            if (resultCode == Activity.RESULT_OK) {
                openLoggedInActivity()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        liveDataObservations.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    override fun openRegisterOptsFrag() {
        replaceFragBackStack(R.id.frame, RegisterOptionsFragment())
    }

    override fun openManualLoginFrag() {
        replaceFragBackStack(R.id.frame, ManualLoginFragment())
    }

    override fun openManualRegisterFrag() {
        replaceFragBackStack(R.id.frame, ManualRegisterFragment())
    }

    private fun observeViewModel() {

        loginVM.loggedInStatus.observe(this, Observer { isLoggedIn: Boolean? ->
            if (isLoggedIn == true) openLoggedInActivity() else openLoginOptsFrag()
        })
        loginVM.registeredStatus.observe(this, Observer { vl: VerifLogin? ->
            if (vl != null) {
                VerifyActivity.startForResult(this, vl, REQ_CODE_VERIFY_REGISTRATION)
            } else {
                openRegisterOptsFrag()
            }
        })
        loginVM.snackBarData.observe(this, Observer { it?.show(binding.frame) })

        @Suppress("UNCHECKED_CAST")
        liveDataObservations.addAll(listOf(
                loginVM.loggedInStatus as LiveData<Any>,
                loginVM.registeredStatus as LiveData<Any>,
                loginVM.snackBarData as LiveData<Any>
        ))
    }

    private fun openLoginOptsFrag() {
        replaceFrag(binding.frame.id, LoginOptionsFragment())
    }

    private fun openLoggedInActivity() {
        //TODO("start ShoppingListsActivity")
        ProfileActivity.start(this)
    }

    companion object {
        const val REQ_CODE_VERIFY_REGISTRATION = 1
    }

}
