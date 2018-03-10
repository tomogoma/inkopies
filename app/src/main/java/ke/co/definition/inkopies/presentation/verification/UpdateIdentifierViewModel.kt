package ke.co.definition.inkopies.presentation.verification

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.Observable
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 09/03/18.
 */
class UpdateIdentifierViewModel @Inject constructor(
        private val resMngr: ResourceManager,
        private val auth: Authable,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val finishedEvent = SingleLiveEvent<VerifLogin>()
    val snackBarData = SingleLiveEvent<SnackBarData>()

    val identifier = ObservableField<String>()
    val identifierError = ObservableField<String>()
    val progressOverlay = ObservableField<ProgressData>()

    init {
        identifier.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val err = identifierError.get()
                if (err != null && !err.isEmpty()) {
                    identifierError.set("")
                }
            }
        })
    }

    fun setIdentifier(id: String) {
        identifier.set(id)
    }

    fun onSubmit() {
        val id = identifier.get()
        if (id == null || id.isEmpty()) {
            identifierError.set(resMngr.getString(R.string.error_required_field))
            return
        }
        auth.updateIdentifier(identifier.get())
                .doOnSubscribe {
                    progressOverlay.set(ProgressData(String.format(
                            resMngr.getString(R.string.updating_login_details_to_ss), id)))
                }
                .doOnUnsubscribe { progressOverlay.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ finishedEvent.value = it }, {
                    snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG)
                })
    }

    class Factory @Inject constructor(
            private val resMngr: ResourceManager,
            private val auth: Authable,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UpdateIdentifierViewModel(resMngr, auth, subscribeOnScheduler, observeOnScheduler)
                    as T
        }

    }
}