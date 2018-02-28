package ke.co.definition.inkopies.views

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentLoginOptionsBinding

class LoginOptionsFragment : Fragment() {


    override fun onCreateView(i: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLoginOptionsBinding>(i,
                R.layout.fragment_login_options, container, false)
        observeView(binding)
        return binding.root
    }

    private fun observeView(b: FragmentLoginOptionsBinding) {
        val coordinator = activity as LoginFragCoordinator
        b.regLink.setOnClickListener({ coordinator.openRegisterOptsFrag() })
        b.loginOptions!!.manualLogin.setOnClickListener({ coordinator.openManualLoginFrag() })
    }

}
