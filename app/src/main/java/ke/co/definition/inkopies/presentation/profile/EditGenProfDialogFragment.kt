package ke.co.definition.inkopies.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogEditGenProfileBinding
import ke.co.definition.inkopies.model.user.Gender
import ke.co.definition.inkopies.model.user.UserProfile
import ke.co.definition.inkopies.presentation.common.SLMDialogFragment

/**
 * Created by tomogoma
 * On 09/03/18.
 */
class EditGenProfDialogFragment : SLMDialogFragment() {

    private var onDismissCallback: (up: UserProfile?) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val views: DialogEditGenProfileBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_edit_gen_profile, container, false)
        val gpvmFactory = (activity!!.application as App).appComponent.generalProfileVMFactory()
        val viewModel = ViewModelProviders.of(this, gpvmFactory)
                .get(GeneralProfileViewModel::class.java)
        views.vm = viewModel

        observeViewModel(viewModel, views)
        observeViews(views, viewModel)
        start(viewModel, views)

        return views.root
    }

    override fun dismiss() {
        super.dismiss()
        onDismissCallback(null)
    }

    fun setOnDismissCallback(cb: (up: UserProfile?) -> Unit) {
        onDismissCallback = cb
    }

    private fun observeViews(vs: DialogEditGenProfileBinding, vm: GeneralProfileViewModel) {
        vs.cancel.setOnClickListener { dismiss() }
        vs.submit.setOnClickListener { onSubmit(vm, vs) }
        vs.gender.setOnCheckedChangeListener { _, _ -> vm.onGenderSelected() }
    }

    private fun observeViewModel(vm: GeneralProfileViewModel, vs: DialogEditGenProfileBinding) {
        vm.finishEvent.observe(this, Observer { dismiss(it) })
        vm.snackbarData.observe(this, Observer { it!!.show(vs.layoutRoot) })

        @Suppress("UNCHECKED_CAST")
        observedLiveData.addAll(mutableListOf(
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
            Gender.NONE -> {
                /* no-op don't bind*/
            }
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