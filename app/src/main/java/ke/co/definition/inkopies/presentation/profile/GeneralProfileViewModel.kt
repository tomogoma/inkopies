package ke.co.definition.inkopies.presentation.profile

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Scheduler
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.user.Gender
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.UserProfile
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
class GeneralProfileViewModel @Inject constructor(
        private val profMngr: ProfileManager,
        private val resMngr: ResourceManager,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val snackbarData: SingleLiveEvent<SnackbarData> = SingleLiveEvent()
    val finishEvent: SingleLiveEvent<UserProfile> = SingleLiveEvent()

    val name: ObservableField<String> = ObservableField()
    val nameError: ObservableField<String> = ObservableField()
    val genderError: ObservableField<String> = ObservableField()
    val progressOverlay: ObservableField<ProgressData> = ObservableField()

    init {
        name.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                val err = nameError.get() ?: return
                if (err == "") return else nameError.set("")
            }
        })
    }

    fun onGenderSelected() {
        genderError.set("")
    }

    fun onSubmit(gender: Gender?) {
        if (!validateGeneralProfile(gender)) {
            return
        }
        profMngr.updateGeneral(name.get()!!, gender!!)
                .doOnSubscribe {
                    progressOverlay.set(ProgressData(resMngr.getString(R.string.updating_profile)))
                }
                .doOnTerminate { progressOverlay.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ finishEvent.value = it }, {
                    snackbarData.value = TextSnackbarData(it, Snackbar.LENGTH_INDEFINITE)
                })
    }

    fun setName(name: String) {
        this.name.set(name)
    }

    private fun validateGeneralProfile(gender: Gender?): Boolean {

        var isValid = true

        val name = name.get()
        if (name == null || name.isEmpty()) {
            nameError.set(resMngr.getString(R.string.error_required_field))
            isValid = false
        }

        if (gender == null) {
            genderError.set(resMngr.getString(R.string.error_required_field))
            isValid = false
        }

        return isValid
    }

    class Factory @Inject constructor(
            private val profMngr: ProfileManager,
            private val resMngr: ResourceManager,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GeneralProfileViewModel(profMngr, resMngr, subscribeOnScheduler, observeOnScheduler)
                    as T
        }

    }
}