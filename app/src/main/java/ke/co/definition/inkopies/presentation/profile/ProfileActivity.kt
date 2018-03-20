package ke.co.definition.inkopies.presentation.profile

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityProfileBinding
import ke.co.definition.inkopies.databinding.ContentProfileBinding
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.model.user.UserProfile
import ke.co.definition.inkopies.presentation.common.loadPic
import ke.co.definition.inkopies.presentation.common.loadProfilePic
import ke.co.definition.inkopies.presentation.common.newRequestListener
import ke.co.definition.inkopies.presentation.verification.ChangeIDDialogFrag
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File


class ProfileActivity : AppCompatActivity() {

    private lateinit var views: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel

    private val liveDataObservers = mutableListOf<LiveData<Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        views = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val pvmFactory = (application as App).appComponent.profileVMFactory()
        viewModel = ViewModelProviders.of(this, pvmFactory).get(ProfileViewModel::class.java)
        views.vm = viewModel

        observeViewModel()
        observeViews(views.content!!)
        viewModel.start()
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, result: Intent?) {
        if (viewModel.onActivityResult(reqCode, resultCode, result)) {
            return
        }
        super.onActivityResult(reqCode, resultCode, result)
    }

    override fun onBackPressed() {
        if (viewModel.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        stopObservingViewModel()
        super.onDestroy()
    }

    private fun observeViews(vs: ContentProfileBinding) {

        vs.avatar.setOnClickListener { viewModel.onEnlargePPic() }
        vs.cameraButton.setOnClickListener { showNewImageOptions() }
        vs.editGenButton.setOnClickListener { showEditGenProfDialog() }
        vs.email.setOnClickListener { showChangeIdentifierDialog(vs.email.text.toString()) }
        vs.phone.setOnClickListener { showChangeIdentifierDialog(vs.phone.text.toString()) }
        vs.googleLink.setOnClickListener { TODO("Open edit identifier dialog") }
        vs.fbLink.setOnClickListener { TODO("Open edit identifier dialog") }
    }

    private fun observeViewModel() {

        viewModel.profileImgURL.observe(this, Observer {
            viewModel.progressProfImg.set(true)
            loadProfilePic(it ?: return@Observer, views.content!!.avatar,
                    newRequestListener { viewModel.progressProfImg.set(false) })
        })
        viewModel.snackbarData.observe(this, Observer { it?.show(views.rootLayout) })
        viewModel.cropImage.observe(this, Observer { TODO() })
        viewModel.loadEnlargedPic.observe(this, Observer {
            loadPic(it ?: return@Observer, views.content!!.bigAvatar, null)
        })
        viewModel.takePhotoEvent.observe(this, Observer {
            openCameraCapture(it ?: return@Observer)
        })

        @Suppress("UNCHECKED_CAST")
        liveDataObservers.addAll(mutableListOf(
                viewModel.profileImgURL as LiveData<Any>,
                viewModel.snackbarData as LiveData<Any>,
                viewModel.cropImage as LiveData<Any>,
                viewModel.takePhotoEvent as LiveData<Any>,
                viewModel.loadEnlargedPic as LiveData<Any>
        ))
    }

    private fun stopObservingViewModel() {
        liveDataObservers.forEach { it.removeObservers(this) }
    }

    private fun showEditGenProfDialog() {
        stopObservingViewModel()
        EditGenProfDialogFragment.start(supportFragmentManager, viewModel.getUserProfile(),
                this@ProfileActivity::onEditUserProfileDismissed)
    }

    private fun onEditUserProfileDismissed(up: UserProfile?) {
        observeViewModel()
        if (up != null) viewModel.setUserProfile(up)
    }

    private fun showChangeIdentifierDialog(currID: String) {
        stopObservingViewModel()
        ChangeIDDialogFrag.start(supportFragmentManager, currID,
                this@ProfileActivity::onChangeIDDialogDismissed)
    }

    private fun onChangeIDDialogDismissed(@Suppress("UNUSED_PARAMETER") vl: VerifLogin?) {
        observeViewModel()
        viewModel.onIdentifierUpdated()
    }

    private fun showNewImageOptions() {
        AlertDialog.Builder(this)
                .setTitle(R.string.profile_photo_title)
                .setItems(R.array.new_image_options, { _, posn -> onNewImageOptionSelected(posn) })
                .show()
    }

    private fun onNewImageOptionSelected(pos: Int) {
        when (pos) {
            resources.getInteger(R.integer.new_image_option_camera_pos) -> viewModel.onReqCaptureCamera()
            else -> openGalleryImageSelect()
        }
    }

    private fun openCameraCapture(cmd: Pair<Int, File>) {

        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (i.resolveActivity(packageManager) == null) {
            Snackbar.make(views.rootLayout, R.string.please_install_camera, Snackbar.LENGTH_LONG)
                    .show()
            return
        }

        val imgURI = FileProvider.getUriForFile(this,
                getString(R.string.file_provider_authorities), cmd.second)
        i.putExtra(MediaStore.EXTRA_OUTPUT, imgURI)
        startActivityForResult(i, cmd.first)
    }

    private fun openGalleryImageSelect() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, ProfileViewModel.REQ_CODE_GALLERY_IMAGE)
    }

    companion object {

        fun start(a: Activity) {
            a.startActivity(Intent(a, ProfileActivity::class.java))
        }
    }
}
