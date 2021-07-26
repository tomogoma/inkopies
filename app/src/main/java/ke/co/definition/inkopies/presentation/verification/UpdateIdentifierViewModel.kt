package ke.co.definition.inkopies.presentation.verification

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Scheduler
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
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
    val snackBarData = SingleLiveEvent<SnackbarData>()

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
        auth.updateIdentifier(id)
                .doOnSubscribe {
                    progressOverlay.set(ProgressData(String.format(
                            resMngr.getString(R.string.updating_login_details_to_ss), id)))
                }
                .doOnTerminate { progressOverlay.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ finishedEvent.value = it }, {
                    snackBarData.value = TextSnackbarData(it.message!!, Snackbar.LENGTH_LONG)
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