package ke.co.definition.inkopies.views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.InkopiesApp
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityLoginBinding

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
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame, RegisterOptionsFragment(), RegisterOptionsFragment::class.java.name)
                .addToBackStack(RegisterOptionsFragment::class.java.name)
                .commit()
    }

    override fun openManualLoginFrag() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame, ManualLoginFragment(), ManualLoginFragment::class.java.name)
                .addToBackStack(ManualLoginFragment::class.java.name)
                .commit()
    }

    override fun openManualRegisterFrag() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame, ManualRegisterFragment(), ManualRegisterFragment::class.java.name)
                .addToBackStack(ManualRegisterFragment::class.java.name)
                .commit()
    }

    private fun observeViewModel() {
        loginVM.loggedInStatus.observe(this, Observer { isLoggedIn: Boolean? ->
            if (isLoggedIn == true) openLoggedInActivity() else showLoginOpts()
        })
    }

    private fun showLoginOpts() {
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        supportFragmentManager.beginTransaction()
                .add(R.id.frame, LoginOptionsFragment(), LoginOptionsFragment::class.java.name)
                .commit()
    }

    private fun openLoggedInActivity() {
        TODO("start ShoppingListsActivity")
    }

}
