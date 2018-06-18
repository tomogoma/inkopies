package ke.co.definition.inkopies.presentation.login

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityLoginBinding
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.presentation.common.InkopiesActivity
import ke.co.definition.inkopies.presentation.shopping.lists.ShoppingListsActivity
import ke.co.definition.inkopies.presentation.verification.VerifyActivity
import java.util.concurrent.atomic.AtomicBoolean


class LoginActivity : InkopiesActivity() {

    private var snackBar: Snackbar? = null
    private lateinit var loginVM: LoginViewModel

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

    override fun onBackPressed() {
        if (binding.content.pager.currentItem == 0) {
            super.onBackPressed()
            return
        }
        binding.content.pager.currentItem -= 1
    }

    private val isLaidOutViews: AtomicBoolean = AtomicBoolean(false)

    private fun layoutViews() {
        if (!isLaidOutViews.compareAndSet(false, true)) {
            return
        }
        observeViews()
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.sign_in_title)
        binding.content.pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
    }

    private fun observeViews() {
        binding.content.next.setOnClickListener {
            when (binding.content.pager.currentItem) {
                ScreenSlidePagerAdapter.IDENTIFIER_PAGE -> binding.vm!!.onIdentifierSubmitted()
                else -> binding.vm!!.onPasswordSubmitted()
            }
        }
        binding.content.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    ScreenSlidePagerAdapter.IDENTIFIER_PAGE -> binding.vm!!.onReadyPresentIdentifier()
                    ScreenSlidePagerAdapter.PASSWORD_PAGE -> binding.vm!!.onReadyPresentPassword()
                }
            }
        })
    }

    private fun observeViewModel() {

        loginVM.loggedInStatus.observe(this, Observer { isLoggedIn: Boolean? ->
            if (isLoggedIn == true) openLoggedInActivity() else layoutViews()
        })
        loginVM.registeredStatus.observe(this, Observer { vl: VerifLogin? ->
            VerifyActivity.startForResult(this, vl!!, REQ_CODE_VERIFY_REGISTRATION)
        })
        loginVM.snackbarData.observe(this, Observer { snackBar = it?.show(binding.rootLayout) })
        loginVM.showPasswordPage.observe(this, Observer { showPasswordPage() })
        loginVM.showIdentifierPage.observe(this, Observer { showIdentifierPage() })

        observedLiveData.addAll(listOf(loginVM.loggedInStatus, loginVM.registeredStatus,
                loginVM.snackbarData, loginVM.showPasswordPage, loginVM.showIdentifierPage))
    }

    private fun showPasswordPage() {
        binding.content.pager.currentItem = ScreenSlidePagerAdapter.PASSWORD_PAGE
    }

    private fun showIdentifierPage() {
        binding.content.pager.currentItem = ScreenSlidePagerAdapter.IDENTIFIER_PAGE
    }

    private fun openLoggedInActivity() {
        ShoppingListsActivity.start(this)
        finish()
    }

    companion object {
        const val REQ_CODE_VERIFY_REGISTRATION = 1

        fun start(activity: InkopiesActivity) {
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                IDENTIFIER_PAGE -> LoginIdentifierFragment()
                else -> LoginPasswordFragment()
            }
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }

        companion object {
            const val IDENTIFIER_PAGE = 0
            const val PASSWORD_PAGE = 1
            const val NUM_PAGES = 2
        }
    }

}
