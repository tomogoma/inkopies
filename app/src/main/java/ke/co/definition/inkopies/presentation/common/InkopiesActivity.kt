package ke.co.definition.inkopies.presentation.common

import android.arch.lifecycle.LiveData
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.presentation.login.LoginActivity
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 31/03/18.
 */
abstract class InkopiesActivity : AppCompatActivity() {

    internal val observedLiveData = mutableListOf<LiveData<*>>()
    private var loggedInStatus = ObservableBoolean()
    private var loggedInStatusChangeCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (loggedInStatus.get() || this@InkopiesActivity is LoginActivity) {
                return // Only interested in change to logged out while on none-LoginActivity.
            }
            LoginActivity.start(this@InkopiesActivity)
            finish()
        }
    }

    @Inject
    lateinit var auth: Authable

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        observeLoggedInStatus()
    }

    override fun onDestroy() {
        loggedInStatus.removeOnPropertyChangedCallback(loggedInStatusChangeCallback)
        removeLiveDataObservers()
        super.onDestroy()
    }

    internal fun removeLiveDataObservers() {
        observedLiveData.forEach { it.removeObservers(this) }
    }

    private fun observeLoggedInStatus() {
        loggedInStatus = auth.observeLoggedInStatus()
        loggedInStatus.addOnPropertyChangedCallback(loggedInStatusChangeCallback)
    }
}