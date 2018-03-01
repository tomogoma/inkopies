package ke.co.definition.inkopies.views

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.databinding.Observable
import android.databinding.ObservableField
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.Validatable
import ke.co.definition.inkopies.model.auth.ValidationResult
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
        val auth: Authable,
        val validation: Validatable,
        @Named(Dagger2Module.SCHEDULER_IO) val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) val observeOnScheduler: Scheduler
) : ViewModel() {

    val loggedInStatus: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val identifier: ObservableField<String> = ObservableField()
    val identifierError: ObservableField<String> = ObservableField()
    val password: ObservableField<String> = ObservableField()
    val passwordError: ObservableField<String> = ObservableField()

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

    fun checkLoggedIn() {
        auth.isLoggedIn()
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(
                        { loggedInStatus.value = it },
                        { throw RuntimeException("Error checking if user is logged in") }
                )
    }

    fun logInManual(c: Context) {

        val pass = password.get()
        val id = identifier.get()
        val valRes = validateLoginDetails(c, id, pass)
        if (!valRes.isValid) {
            return
        }

        auth.loginManual(valRes.getIdentifier(), pass)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(
                        { loggedInStatus.value = true },
                        { /* TODO handle error */ }
                )

    }

    fun registerManual(c: Context) {

        val pass = password.get()
        val id = identifier.get()
        val valRes = validateLoginDetails(c, id, pass)
        if (!valRes.isValid) {
            return
        }

        TODO("Handle registration")

    }

    private fun validateLoginDetails(c: Context, id: String?, pass: String?): ValidationResult {

        var result: ValidationResult? = null

        if (!validation.isValidPassword(pass)) {
            passwordError.set(c.getString(R.string.error_password_too_short))
            result = ValidationResult.Invalid()
        }

        val idRes = validation.validateIdentifier(c, id)
        return if (idRes.isValid) {
            if (result == null) idRes else result
        } else {
            identifierError.set(c.getString(R.string.error_bad_email_or_phone))
            ValidationResult.Invalid()
        }
    }

    class Factory @Inject constructor(
            val auth: Authable,
            val validation: Validatable,
            @Named(Dagger2Module.SCHEDULER_IO) val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(auth, validation, subscribeOnScheduler, observeOnScheduler) as T
        }

    }

}