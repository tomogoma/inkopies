package ke.co.definition.inkopies.presentation.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
        // keep this separate from removeLiveDataObservers to
        // ensure it is only removed onDestroy()
        viewModel.loggedInStatus.removeObservers(this)
        removeLiveDataObservers()
        super.onDestroy()
    }

    internal fun requestWriteExtFilePerm() =
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERM_REQ_WRITE_EXT_STORAGE)

    internal fun shouldShowWriteExtFileRationale() =
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)

    internal fun haveWriteExtFilePerms() =
            (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)

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

        observedLiveData.add(viewModel.toastData /*loggedInStatus is not added, as it should
         only be removed on destroy*/)
    }

    companion object {

        const val PERM_REQ_WRITE_EXT_STORAGE = 0
    }
}