package ke.co.definition.inkopies.presentation.login

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentLoginPasswordBinding
import ke.co.definition.inkopies.presentation.common.InkopiesFragment
import ke.co.definition.inkopies.presentation.common.loadProfilePic
import ke.co.definition.inkopies.presentation.common.newRequestListener

class LoginPasswordFragment : InkopiesFragment() {

    private lateinit var lvm: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLoginPasswordBinding>(inflater,
                R.layout.fragment_login_password, container, false)

        val lvmFactory = (activity!!.application as App).appComponent.loginVMFactory()
        lvm = ViewModelProviders.of(activity!!, lvmFactory).get(LoginViewModel::class.java)
        binding.vm = lvm

        observeView(binding)
        observeViewModel(binding)

        return binding.root
    }

    private fun observeViewModel(b: FragmentLoginPasswordBinding) {
        b.vm!!.avatarURL.observe(this, Observer {
            if (it == null) {
                b.avatar.setImageResource(R.drawable.avatar)
            } else {
                b.vm!!.progressProfImg.set(true)
                (activity as AppCompatActivity)
                        .loadProfilePic(it, b.avatar,
                                newRequestListener { b.vm!!.progressProfImg.set(false) })
            }
        })
        observedLiveData.add(b.vm!!.avatarURL)
    }

    private fun observeView(b: FragmentLoginPasswordBinding) {
        b.forgotPassword.setOnClickListener({ b.vm!!.forgotPassword() })
        b.password.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                b.vm!!.onPasswordSubmitted()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

}
