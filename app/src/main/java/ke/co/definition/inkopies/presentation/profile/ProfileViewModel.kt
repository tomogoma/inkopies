package ke.co.definition.inkopies.presentation.profile

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.FileHelper
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.UserProfile
import ke.co.definition.inkopies.presentation.common.ResIDSnackBarData
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 06/03/18.
 */
class ProfileViewModel @Inject constructor(
        private val profMngr: ProfileManager,
        private val resMngr: ResourceManager,
        private val fileHelper: FileHelper,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val profileImgURL = SingleLiveEvent<String>()
    val snackbarData = SingleLiveEvent<SnackBarData>()
    val cropImage = SingleLiveEvent<Boolean>()
    val loadEnlargedPic = SingleLiveEvent<String>()
    val takePhotoEvent = SingleLiveEvent<Pair<Int, File>>()

    val userProfile = ObservableField<UserProfile>()
    val googleLinkText = ObservableField<String>()
    val fbLinkText = ObservableField<String>()
    val progressTopBar = ObservableField<Boolean>()
    val progressProfImg = ObservableField<Boolean>()
    val enlargePPic = ObservableField<Boolean>()

    fun start() {
        profMngr.getUser()
                .doOnSubscribe { progressTopBar.set(true) }
                .doOnUnsubscribe { progressTopBar.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ onUserProfileLoaded(it) }, {
                    snackbarData.value = TextSnackBarData(it, Snackbar.LENGTH_INDEFINITE)
                })
    }

    fun setUserProfile(up: UserProfile) {
        onUserProfileLoaded(up)
    }

    fun getUserProfile(): UserProfile? = userProfile.get()

    fun onIdentifierUpdated() {
        start()
    }

    fun onReqCaptureCamera() {
        val img = fileHelper.createFile()
        takePhotoEvent.value = Pair(REQ_CODE_CAM_CAPTURE_IMAGE, img)
    }

    fun onActivityResult(reqCode: Int, resultCode: Int, result: Intent?): Boolean {

        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return false
        }

        if (reqCode == REQ_CODE_CAM_CAPTURE_IMAGE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                snackbarData.value = ResIDSnackBarData(R.string.error_something_wicked,
                        Snackbar.LENGTH_LONG)
                return true
            }
            val currUP = userProfile.get() ?: return true
            val newPPicURL = takePhotoEvent.value?.second!!.absolutePath ?: return false
            onUserProfileLoaded(UserProfile(currUP.auth, currUP.name, currUP.gender, newPPicURL))
            uploadProfileImage(newPPicURL)
            return true
        }

        if (reqCode == REQ_CODE_GALLERY_IMAGE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                snackbarData.value = ResIDSnackBarData(R.string.error_something_wicked,
                        Snackbar.LENGTH_LONG)
                return true
            }
            val currUP = userProfile.get() ?: return true
            val newPPicURL = (result?.data ?: return false).toString()
            onUserProfileLoaded(
                    UserProfile(currUP.auth, currUP.name, currUP.gender, newPPicURL))
            uploadProfileImage(newPPicURL)
            return true
        }

        return false
    }

    fun onEnlargePPic() {
        val up = userProfile.get()
        if (up == null || up.imageURL.isEmpty()) {
            return
        }
        enlargePPic.set(true)
        loadEnlargedPic.value = up.imageURL
    }

    fun onBackPressed(): Boolean {
        if (enlargePPic.get()) {
            enlargePPic.set(false)
            return true
        }
        return false
    }

    private fun uploadProfileImage(uri: String) {
        profMngr.uploadProfilePic(uri)
                .doOnSubscribe { progressProfImg.set(true) }
                .doOnUnsubscribe { progressProfImg.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ onUserProfileLoaded(it) }, {
                    snackbarData.value = TextSnackBarData(it, Snackbar.LENGTH_INDEFINITE)
                })
    }

    private fun onUserProfileLoaded(it: UserProfile) {
        userProfile.set(it)
        profileImgURL.value = it.imageURL
        googleLinkText.set(resMngr.getString(R.string.link_google))
        fbLinkText.set(resMngr.getString(R.string.link_facebook))
    }

    companion object {
        const val REQ_CODE_GALLERY_IMAGE = 2
        private const val REQ_CODE_CAM_CAPTURE_IMAGE = 1
    }

    class Factory @Inject constructor(
            private val profMngr: ProfileManager,
            private val resMngr: ResourceManager,
            private val fileHelper: FileHelper,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProfileViewModel(profMngr, resMngr, fileHelper, subscribeOnScheduler, observeOnScheduler) as T
        }

    }
}