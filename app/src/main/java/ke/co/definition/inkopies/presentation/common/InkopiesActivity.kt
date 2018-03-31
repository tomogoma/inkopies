package ke.co.definition.inkopies.presentation.common

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.presentation.login.LoginActivity

/**
 * Created by tomogoma
 * On 31/03/18.
 */
abstract class InkopiesActivity : AppCompatActivity() {

    internal val observedLiveData = mutableListOf<LiveData<*>>()

    private var loggedInStatusObserverPos = -1L

    private lateinit var auth: Authable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = (application as App).appComponent.provideAuthable()
        observeLoggedInStatus()
    }

    override fun onDestroy() {
        auth.unRegisterLoggedInStatusObserver(loggedInStatusObserverPos)
        removeLiveDataObservers()
        super.onDestroy()
    }

    internal fun removeLiveDataObservers() {
        observedLiveData.forEach { it.removeObservers(this) }
    }

    private fun onLoggedInStatusChange(newStatus: Boolean) {
        if (newStatus || this@InkopiesActivity is LoginActivity) {
            return // Only interested in change to logged out while on none-LoginActivity.
        }
        runOnUiThread {
            LoginActivity.start(this@InkopiesActivity)
            finish()
        }
    }

    private fun observeLoggedInStatus() {
        loggedInStatusObserverPos = auth.registerLoggedInStatusObserver(this::onLoggedInStatusChange)
    }
}