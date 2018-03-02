package ke.co.definition.inkopies.presentation.login

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentManualRegisterBinding

class ManualRegisterFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentManualRegisterBinding>(inflater,
                R.layout.fragment_manual_register, container, false)

        val lvmFactory = (activity.application as App).appComponent.loginVMFactory()
        val lvm = ViewModelProviders.of(activity, lvmFactory).get(LoginViewModel::class.java)
        binding.vm = lvm

        observeView(binding)

        return binding.root
    }

    private fun observeView(b: FragmentManualRegisterBinding) {
        b.submit.setOnClickListener({ b.vm!!.registerManual(context) })
        b.manualLoginInputs!!.password.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                b.vm!!.logInManual(context)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

}
