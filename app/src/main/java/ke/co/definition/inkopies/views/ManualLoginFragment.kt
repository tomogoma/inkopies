package ke.co.definition.inkopies.views

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ke.co.definition.inkopies.InkopiesApp
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentManualLoginBinding

class ManualLoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentManualLoginBinding>(inflater,
                R.layout.fragment_manual_login, container, false)

        val lvmFactory = (activity.application as InkopiesApp).appComponent.loginVMFactory()
        val lvm = ViewModelProviders.of(this, lvmFactory).get(LoginViewModel::class.java)
        binding.vm = lvm

        observeView(binding)

        return binding.root
    }

    private fun observeView(b: FragmentManualLoginBinding) {
        b.submit.setOnClickListener({ b.vm!!.logInManual(context) })
    }

}
