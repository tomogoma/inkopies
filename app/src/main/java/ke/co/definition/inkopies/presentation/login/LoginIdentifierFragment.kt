package ke.co.definition.inkopies.presentation.login

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentLoginIdentifierBinding
import ke.co.definition.inkopies.presentation.common.InkopiesFragment

class LoginIdentifierFragment : InkopiesFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLoginIdentifierBinding>(inflater,
                R.layout.fragment_login_identifier, container, false)

        val lvmFactory = (activity!!.application as App).appComponent.loginVMFactory()
        val lvm = ViewModelProviders.of(activity!!, lvmFactory).get(LoginViewModel::class.java)
        binding.vm = lvm

        observeView(binding)

        return binding.root
    }

    private fun observeView(b: FragmentLoginIdentifierBinding) {
        b.identifier.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                b.vm!!.onIdentifierSubmitted()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }
}