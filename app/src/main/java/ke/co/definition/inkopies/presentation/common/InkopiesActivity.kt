package ke.co.definition.inkopies.presentation.common

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.presentation.login.LoginActivity
import ke.co.definition.inkopies.presentation.profile.ProfileActivity

/**
 * Created by tomogoma
 * On 31/03/18.
 */
abstract class InkopiesActivity : AppCompatActivity() {

    internal val observedLiveData = mutableListOf<LiveData<*>>()

    private lateinit var viewModel: InkopiesActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vmFactory = (application as App).appComponent.provideInkopiesActivityVMFactory()
        viewModel = ViewModelProviders.of(this, vmFactory)
                .get(InkopiesActivityViewModel::class.java)
        observeViewModel()
        viewModel.start()
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        viewModel.loggedInStatus.removeObservers(this)
        removeLiveDataObservers()
        super.onDestroy()
    }

    internal fun removeLiveDataObservers() {
        observedLiveData.forEach { it.removeObservers(this) }
    }

    internal fun logout() {
        viewModel.onLogout({}, {})
    }

    internal fun showProfile() {
        ProfileActivity.start(this)
    }

    private fun observeViewModel() {
        viewModel.toastData.observe(this, Observer {
            Toast.makeText(this, it?.first ?: return@Observer, it.second).show()
        })

        viewModel.loggedInStatus.observe(this, Observer {
            if (it == true || this@InkopiesActivity is LoginActivity) {
                return@Observer // Only interested in change to logged out while on none-LoginActivity.
            }
            LoginActivity.start(this@InkopiesActivity)
            finish()
        })

        observedLiveData.add(viewModel.toastData /*loggedInStatus is not observed, as it should
         only be removed on destroy*/)
    }
}