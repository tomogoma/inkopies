package ke.co.definition.inkopies.presentation.profile

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.databinding.ObservableField
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.theartofdev.edmodo.cropper.CropImage
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.UserProfile
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.ResIDSnackBarData
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 06/03/18.
 */
class ProfileViewModel @Inject constructor(
        private val profMngr: ProfileManager,
        private val resMngr: ResourceManager,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val profileImgURL = SingleLiveEvent<Uri>()
    val snackbarData = SingleLiveEvent<SnackBarData>()
    val cropImage = SingleLiveEvent<Boolean>()

    val userProfile = ObservableField<UserProfile>()
    val googleLinkText = ObservableField<String>()
    val fbLinkText = ObservableField<String>()
    val loading = ObservableField<Boolean>()
    val updatedName = ObservableField<String>()
    val updatedNameError = ObservableField<String>()
    val genderError = ObservableField<String>()
    val progress = ObservableField<ProgressData>()

    fun start() {
        profMngr.getUser()
                .doOnSubscribe { loading.set(true) }
                .doOnUnsubscribe { loading.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ onUserProfileLoaded(it) }, {
                    snackbarData.value = TextSnackBarData(it, Snackbar.LENGTH_INDEFINITE)
                })
    }

    fun saveGeneralProfile(gender: String?) {
        if (!validateGeneralProfile(gender)) {
            return
        }
        profMngr.updateUser(updatedName.get(), gender!!, profileImgURL.value.toString())
                .doOnSubscribe {
                    progress.set(ProgressData(resMngr.getString(R.string.updating_profile)))
                }
                .doOnUnsubscribe { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ onUserProfileLoaded(it) }, {
                    snackbarData.value = TextSnackBarData(it, Snackbar.LENGTH_INDEFINITE)
                })
    }

    fun onActivityResult(reqCode: Int, resultCode: Int, result: Intent?): Boolean {

        if (reqCode == REQ_CODE_CAPTURE_IMAGE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                snackbarData.value = ResIDSnackBarData(R.string.error_something_wicked,
                        Snackbar.LENGTH_LONG)
                return true
            }
            cropImage.value = true
            return true
        }

        if (reqCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val data = CropImage.getActivityResult(result)

            if (resultCode != Activity.RESULT_OK) {
                if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    snackbarData.value = TextSnackBarData(data.error, Snackbar.LENGTH_LONG)
                    return true
                }
                snackbarData.value = ResIDSnackBarData(R.string.error_something_wicked,
                        Snackbar.LENGTH_LONG)
                return true
            }

            profileImgURL.value = data.uri
            return true
        }

        return false
    }

    private fun onUserProfileLoaded(it: UserProfile) {
        userProfile.set(it)
        googleLinkText.set(resMngr.getString(R.string.link_google))
        fbLinkText.set(resMngr.getString(R.string.link_facebook))
    }

    private fun validateGeneralProfile(gender: String?): Boolean {

        var isValid = true

        val name = updatedName.get()
        if (name == null || name.isEmpty()) {
            updatedNameError.set(resMngr.getString(R.string.error_required_field))
            isValid = false
        }

        if (gender == null || gender.isEmpty()) {
            genderError.set(resMngr.getString(R.string.error_required_field))
            isValid = false
        }

        return isValid
    }

    companion object {
        internal const val REQ_CODE_CAPTURE_IMAGE = 1
    }

    class Factory @Inject constructor(
            private val profMngr: ProfileManager,
            private val resMngr: ResourceManager,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProfileViewModel(profMngr, resMngr, subscribeOnScheduler, observeOnScheduler) as T
        }

    }
}