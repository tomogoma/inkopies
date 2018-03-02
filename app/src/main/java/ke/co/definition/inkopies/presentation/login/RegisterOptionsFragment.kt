package ke.co.definition.inkopies.presentation.login


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentRegisterOptionsBinding


/**
 * A simple [Fragment] subclass.
 */
class RegisterOptionsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentRegisterOptionsBinding>(inflater,
                R.layout.fragment_register_options, container, false)
        observeView(binding)
        return binding.root
    }

    private fun observeView(b: FragmentRegisterOptionsBinding) {
        val coordinator = activity as LoginFragCoordinator
        b.loginOptions!!.manualLogin.setOnClickListener({ coordinator.openManualRegisterFrag() })
    }

}
