package ke.co.definition.inkopies.presentation.login

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.model.GlideUrl
import com.google.android.material.snackbar.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.*
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.PubUserProfile
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by tomogoma
 * On 28/02/18.
 */
class LoginViewModel @Inject constructor(
        private val auth: Authable,
        private val user: ProfileManager,
        private val validation: Validatable,
        private val resMan: ResourceManager,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val loggedInStatus = SingleLiveEvent<Boolean>()
    val registeredStatus = SingleLiveEvent<VerifLogin>()
    val snackbarData = SingleLiveEvent<SnackbarData>()
    val showPasswordPage = SingleLiveEvent<Unit>()
    val showIdentifierPage = SingleLiveEvent<Unit>()
    val avatarURL = SingleLiveEvent<GlideUrl>()

    val progressData = ObservableField<ProgressData>()
    val identifier = ObservableField<String>()
    val identifierError = ObservableField<String>()
    val password = ObservableField<String>()
    val passwordError = ObservableField<String>()
    val pubUserProfile = ObservableField<PubUserProfile>()
    val isRegistered: ObservableBoolean = ObservableBoolean()
    val progressProfImg: ObservableBoolean = ObservableBoolean()

    private var identifierRslt: Identifier? = null

    init {
        identifier.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val err = identifierError.get()
                if (err != null && !err.isEmpty()) identifierError.set("")
            }
        })
        password.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val err = passwordError.get()
                if (err != null && !err.isEmpty()) passwordError.set("")
            }
        })
    }

    fun onIdentifierSubmitted() {

        val idRslt = validateIdentifier() ?: return
        identifierRslt = idRslt
        auth.getUserID(idRslt)
                .doOnSubscribe {
                    val fmt = resMan.getString(R.string.checking_ss)
                    val fmtArg = idRslt.type().toLowerCase()
                    progressData.set(ProgressData(String.format(fmt, fmtArg)))
                }
                .doOnUnsubscribe { progressData.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onFetchUserID, this::handleError)
    }

    fun onReadyPresentIdentifier() {
        clearIdentifierRslt()
    }

    fun onReadyPresentPassword() {
        val idRslt = validateIdentifier()
        if (idRslt == null) {
            clearIdentifierRslt()
            showIdentifierPage.call()
            return
        }
        if (identifierRslt == null) {
            onIdentifierSubmitted()
            return
        }
    }

    fun changeIdentifier() {
        clearIdentifierRslt()
        showIdentifierPage.call()
    }

    fun onPasswordSubmitted() {
        val idRslt = identifierRslt
        if (idRslt == null) {
            clearIdentifierRslt()
            showIdentifierPage.call()
            return
        }
        val pass = password.get()
        if (!validation.isValidPassword(pass)) {
            passwordError.set(resMan.getString(R.string.error_password_too_short))
            return
        }
        if (isRegistered.get()) {
            logInManual(idRslt, pass!!)
        } else {
            registerManual(idRslt, pass!!)
        }
    }

    fun forgotPassword() {
        TODO()
    }

    fun checkLoggedIn() {
        auth.isLoggedIn()
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onLoginStatus,
                        { throw RuntimeException("Error checking if user is logged in", it) }
                )
    }

    private fun clearIdentifierRslt() {
        avatarURL.value = null
        pubUserProfile.set(null)
        identifierRslt = null
    }

    private fun validateIdentifier(): Identifier? {
        val id = identifier.get()
        val rslt = validation.validateIdentifier(id)
        if (!rslt.isValid) {
            identifierError.set(resMan.getString(R.string.error_bad_email_or_phone))
            return null
        }
        return rslt.getIdentifier()
    }

    private fun onLoginStatus(status: LoggedInStatus) {
        if (!status.loggedIn) {
            loggedInStatus.value = false
            return
        }
        if (!status.verified) {
            registeredStatus.value = status.verifLogin
            return
        }
        loggedInStatus.value = true
    }

    private fun logInManual(ider: Identifier, pass: String) {
        auth.loginManual(ider, pass)
                .doOnSubscribe({ progressData.set(ProgressData(resMan.getString(R.string.loggin_in))) })
                .doOnUnsubscribe({ progressData.set(ProgressData()) })
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onLoginStatus, this::handleError)
    }

    private fun registerManual(ider: Identifier, pass: String) {
        auth.registerManual(ider, pass)
                .doOnSubscribe({ progressData.set(ProgressData(resMan.getString(R.string.registering))) })
                .doOnUnsubscribe({ progressData.set(ProgressData()) })
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ registeredStatus.value = it }, this::handleErrorIndefinite)
    }

    private fun onFetchUserID(userID: String) {
        if (userID.isEmpty()) {
            isRegistered.set(false)
            pubUserProfile.set(null)
        } else {
            isRegistered.set(true)
            fetchPubUser(userID)
        }
        avatarURL.value = null
        showPasswordPage.call()
    }

    private fun fetchPubUser(userID: String) {
        user.getPubUser(userID)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(this::onFetchPubUser, this::handleError)
    }

    private fun onFetchPubUser(pp: PubUserProfile) {
        pubUserProfile.set(pp)
        loadAvatarURL()
    }

    private fun loadAvatarURL() {
        val url = pubUserProfile.get()?.avatarURL ?: return
        auth.glideURL(url)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ avatarURL.value = it }, { /* no-op */ })
    }

    private fun handleError(ex: Throwable) {
        snackbarData.value = TextSnackbarData(ex.message!!, Snackbar.LENGTH_LONG)
    }

    private fun handleErrorIndefinite(ex: Throwable) {
        snackbarData.value = TextSnackbarData(ex.message!!, Snackbar.LENGTH_INDEFINITE)
    }

    class Factory @Inject constructor(
            private val auth: Authable,
            private val user: ProfileManager,
            private val validation: Validatable,
            private val resMan: ResourceManager,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(auth, user, validation, resMan, subscribeOnScheduler,
                    observeOnScheduler) as T
        }

    }

}