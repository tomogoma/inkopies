package ke.co.definition.inkopies.presentation.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.model.GlideUrl
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.FileHelper
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.user.ProfileManager
import ke.co.definition.inkopies.model.user.UserProfile
import ke.co.definition.inkopies.presentation.common.ResIDSnackbarData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
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
        private val authCl: Authable,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val profileImgURL = SingleLiveEvent<GlideUrl>()
    val snackbarData = SingleLiveEvent<SnackbarData>()
    val cropImage = SingleLiveEvent<Uri>()
    val loadEnlargedPic = SingleLiveEvent<GlideUrl>()
    val takePhotoEvent = SingleLiveEvent<Pair<Int, File>>()

    val userProfile = ObservableField<UserProfile>()
    val googleLinkText = ObservableField<String>()
    val fbLinkText = ObservableField<String>()
    val progressTopBar = ObservableBoolean()
    val progressProfImg = ObservableBoolean()
    val enlargePPic = ObservableBoolean(false)

    fun start() {
        profMngr.getUser()
                .doOnSubscribe { progressTopBar.set(true) }
                .doOnUnsubscribe { progressTopBar.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ onUserProfileLoaded(it) }, {
                    snackbarData.value = TextSnackbarData(it, Snackbar.LENGTH_INDEFINITE)
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
        val img = fileHelper.createTempFile(".jpg")
        takePhotoEvent.value = Pair(REQ_CODE_CAM_CAPTURE_IMAGE, img)
    }

    fun onActivityResult(reqCode: Int, resultCode: Int, result: Intent?): Boolean {

        if (!arrayOf(REQ_CODE_CAM_CAPTURE_IMAGE, REQ_CODE_GALLERY_IMAGE,
                        CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE).contains(reqCode)) {
            return false
        }

        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return true
        }

        if (resultCode != AppCompatActivity.RESULT_OK) {
            snackbarData.value = ResIDSnackbarData(R.string.error_something_wicked,
                    Snackbar.LENGTH_LONG)
            return true
        }

        if (reqCode == REQ_CODE_CAM_CAPTURE_IMAGE) {
            val camPhotoUri = Uri.fromFile(takePhotoEvent.value?.second!!) ?: return false
            cropImage.postValue(camPhotoUri)
            return true
        }

        if (reqCode == REQ_CODE_GALLERY_IMAGE) {
            val newPPicURL = result?.data ?: return false
            cropImage.postValue(newPPicURL)
            return true
        }

        if (reqCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val cropRslt = CropImage.getActivityResult(result)
            uploadProfileImage(cropRslt.uri)
            return true
        }

        return false
    }

    fun onEnlargePPic() {
        val up = userProfile.get()
        if (up == null || up.avatarURL.isEmpty()) {
            return
        }
        enlargePPic.set(true)
        authCl.glideURL(up.avatarURL)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ loadEnlargedPic.value = it }, { /*no-op*/ })
    }

    fun onBackPressed(): Boolean {
        if (enlargePPic.get()) {
            enlargePPic.set(false)
            return true
        }
        return false
    }

    private fun uploadProfileImage(uri: Uri) {
        profMngr.uploadProfilePic(uri)
                .doOnSubscribe { progressProfImg.set(true) }
                .doOnUnsubscribe { progressProfImg.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ onUserProfileLoaded(it) }, {
                    snackbarData.value = TextSnackbarData(it, Snackbar.LENGTH_INDEFINITE)
                })
    }

    private fun onUserProfileLoaded(up: UserProfile) {
        userProfile.set(up)
        googleLinkText.set(resMngr.getString(R.string.link_google))
        fbLinkText.set(resMngr.getString(R.string.link_facebook))
        authCl.glideURL(up.avatarURL)
                .doOnSubscribe { progressProfImg.set(true) }
                .doOnUnsubscribe { progressProfImg.set(false) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ profileImgURL.value = it }, { /*no-op*/ })
    }

    companion object {
        const val REQ_CODE_GALLERY_IMAGE = 2
        private const val REQ_CODE_CAM_CAPTURE_IMAGE = 1
    }

    class Factory @Inject constructor(
            private val profMngr: ProfileManager,
            private val resMngr: ResourceManager,
            private val fileHelper: FileHelper,
            private val authCl: Authable,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProfileViewModel(profMngr, resMngr, fileHelper, authCl, subscribeOnScheduler,
                    observeOnScheduler) as T
        }

    }
}