package ke.co.definition.inkopies.presentation.profile

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityProfileBinding
import ke.co.definition.inkopies.databinding.ChangeIdentifierDialogBinding
import ke.co.definition.inkopies.databinding.ContentProfileBinding
import ke.co.definition.inkopies.databinding.EditGenProfileDialogBinding
import ke.co.definition.inkopies.presentation.verification.VerificationViewModel
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var views: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        views = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        observeViews(views.content!!)
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, result: Intent?) {

        super.onActivityResult(reqCode, resultCode, result)
    }

    private fun observeViews(vs: ContentProfileBinding) {

        vs.avatar.setOnClickListener { TODO("Show enlarged image") }
        vs.cameraButton.setOnClickListener { showNewImageOptions() }
        vs.editGenButton.setOnClickListener { TODO("Open edit gen profile dialog") }
        vs.email.setOnClickListener { TODO("Open edit identifier dialog") }
        vs.phone.setOnClickListener { TODO("Open edit identifier dialog") }
        vs.googleLink.setOnClickListener { TODO("Open edit identifier dialog") }
        vs.fbLink.setOnClickListener { TODO("Open edit identifier dialog") }
    }

    private fun showNewImageOptions() {
        AlertDialog.Builder(this)
                .setTitle(R.string.profile_photo_title)
                .setItems(R.array.new_image_options, { _, posn -> onNewImageOptionSelected(posn) })
                .show()
    }

    private fun onNewImageOptionSelected(pos: Int) {
        when (pos) {
            resources.getInteger(R.integer.new_image_option_camera_pos) -> openCameraCapture()
            else -> openGalleryImageSelect()
        }
    }

    private fun openCameraCapture() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, ProfileViewModel.REQ_CODE_CAPTURE_IMAGE)
    }

    private fun openGalleryImageSelect() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, ProfileViewModel.REQ_CODE_CAPTURE_IMAGE)
    }

    companion object {

        fun start(a: Activity) {
            a.startActivity(Intent(a, ProfileActivity::class.java))
        }
    }


    class ChangeIDDialogFrag : DialogFragment() {

        private val observedLiveData: MutableList<LiveData<Any>> = mutableListOf()
        private var onDismissCallback: () -> Unit = {}

        override fun onCreateView(i: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val views = DataBindingUtil.inflate<EditGenProfileDialogBinding>(i,
                    R.layout.edit_gen_profile_dialog, container, false)

            val pvmFactory = (activity.application as App).appComponent.verificationVMFactory()
            val viewModel = ViewModelProviders.of(activity, pvmFactory)
                    .get(VerificationViewModel::class.java)
            views.vm = viewModel

            observeViews(views)
            observeViewModel(viewModel, views)

            return views.root
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            return dialog
        }

        override fun onDestroy() {
            observedLiveData.forEach { it.removeObservers(this) }
            super.onDestroy()
        }

        override fun onDismiss(dialog: DialogInterface?) {
            onDismissCallback()
            super.onDismiss(dialog)
        }

        fun setOnDismissCallback(cb: () -> Unit) {
            onDismissCallback = cb
        }

        private fun observeViewModel(vm: VerificationViewModel, vs: ChangeIdentifierDialogBinding) {

            vm.finishedChangeIdentifierEv.observe(this, Observer { dialog.dismiss() })
            vm.snackBarData.observe(this, Observer { it?.show(vs.layoutRoot) })

            @Suppress("UNCHECKED_CAST")
            observedLiveData.addAll(mutableListOf(
                    vm.finishedChangeIdentifierEv as LiveData<Any>,
                    vm.snackBarData as LiveData<Any>
            ))
        }

        private fun observeViews(vs: ChangeIdentifierDialogBinding) {
            vs.identifier.setOnEditorActionListener({ _, actionID, _ ->
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    vs.vm!!.onSubmitChangeIdentifier()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            })
            vs.submit.setOnClickListener({ vs.vm!!.onSubmitChangeIdentifier() })
            vs.cancel.setOnClickListener({ dialog.dismiss() })
        }
    }
}
