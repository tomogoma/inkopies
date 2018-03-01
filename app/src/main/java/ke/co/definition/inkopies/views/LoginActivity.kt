package ke.co.definition.inkopies.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.InkopiesApp
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityLoginBinding
import ke.co.definition.inkopies.views.common.replaceFrag
import ke.co.definition.inkopies.views.common.replaceFragBackStack

class LoginActivity : AppCompatActivity(), LoginFragCoordinator {

    private lateinit var loginVM: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lvmFactory = (application as InkopiesApp).appComponent.loginVMFactory()
        loginVM = ViewModelProviders.of(this, lvmFactory).get(LoginViewModel::class.java)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        loginVM.checkLoggedIn()
    }

    override fun onDestroy() {
        loginVM.loggedInStatus.removeObservers(this)
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
            if (isLoggedIn == true) openLoggedInActivity() else showLoginOpts()
        })
    }

    private fun showLoginOpts() {
        // We only load the layout if not logged in (hence need to log in)
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        replaceFrag(R.id.frame, LoginOptionsFragment())
    }

    private fun openLoggedInActivity() {
        TODO("start ShoppingListsActivity")
    }

}
