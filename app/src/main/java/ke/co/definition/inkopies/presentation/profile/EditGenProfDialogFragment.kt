package ke.co.definition.inkopies.presentation.profile

import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogEditGenProfileBinding
import ke.co.definition.inkopies.model.user.Gender
import ke.co.definition.inkopies.model.user.UserProfile

/**
 * Created by tomogoma
 * On 09/03/18.
 */
class EditGenProfDialogFragment : DialogFragment() {

    private var isDialog = false
    private var onDismissCallback: (up: UserProfile?) -> Unit = {}

    private val liveDataObservers = mutableListOf<LiveData<Any>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val views: DialogEditGenProfileBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_edit_gen_profile, container, false)
        val gpvmFactory = (activity.application as App).appComponent.generalProfileVMFactory()
        val viewModel = ViewModelProviders.of(this, gpvmFactory)
                .get(GeneralProfileViewModel::class.java)

        observeViewModel(viewModel, views)
        observeViews(views, viewModel)
        start(viewModel, views)

        return views.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        isDialog = true
        return dialog
    }

    override fun dismiss() {
        super.dismiss()
        onDismissCallback(null)
    }

    override fun onDestroy() {
        liveDataObservers.forEach { it.removeObservers(this) }
        super.onDestroy()
    }

    fun setOnDismissCallback(cb: (up: UserProfile?) -> Unit) {
        onDismissCallback = cb
    }

    private fun observeViews(vs: DialogEditGenProfileBinding, vm: GeneralProfileViewModel) {
        vs.cancel.setOnClickListener { dismiss() }
        vs.submit.setOnClickListener { onSubmit(vm, vs) }
    }

    private fun observeViewModel(vm: GeneralProfileViewModel, vs: DialogEditGenProfileBinding) {
        vm.finishEvent.observe(this, Observer { dismiss(it) })
        vm.snackbarData.observe(this, Observer { it!!.show(vs.layoutRoot) })

        @Suppress("UNCHECKED_CAST")
        liveDataObservers.addAll(mutableListOf(
                vm.finishEvent as LiveData<Any>,
                vm.snackbarData as LiveData<Any>
        ))
    }

    private fun start(vm: GeneralProfileViewModel, vs: DialogEditGenProfileBinding) {

        val usrProfStr = arguments?.getString(EXTRA_USER_PROFILE) ?: return
        val usrProf = Gson().fromJson(usrProfStr, UserProfile::class.java)

        when (usrProf.gender) {
            Gender.MALE -> vs.gender.check(R.id.male)
            Gender.FEMALE -> vs.gender.check(R.id.female)
            Gender.OTHER -> vs.gender.check(R.id.other)
        }
        vm.setName(usrProf.name)
    }

    private fun onSubmit(vm: GeneralProfileViewModel, vs: DialogEditGenProfileBinding) {
        val gender = when (vs.gender.checkedRadioButtonId) {
            R.id.male -> Gender.MALE
            R.id.female -> Gender.FEMALE
            R.id.other -> Gender.OTHER
            else -> null
        }
        vm.onSubmit(gender)
    }

    private fun dismiss(up: UserProfile?) {
        super.dismiss()
        onDismissCallback(up)
    }

    companion object {
        private val EXTRA_USER_PROFILE = EditGenProfDialogFragment::class.java.name +
                ".EXTRA_USER_PROFILE"

        fun start(fm: FragmentManager, up: UserProfile?,
                  onDismissCallback: (vl: UserProfile?) -> Unit = {}) {
            EditGenProfDialogFragment().apply {
                setOnDismissCallback(onDismissCallback)
                if (up != null) {
                    arguments = Bundle().apply { putString(EXTRA_USER_PROFILE, Gson().toJson(up)) }
                }
                isCancelable = false
                show(fm, EditGenProfDialogFragment::class.java.name)
            }
        }
    }
}