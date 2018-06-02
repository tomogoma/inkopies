package ke.co.definition.inkopies.presentation.shopping.checkout

import android.app.DatePickerDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import ke.co.definition.inkopies.App
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.DialogCheckoutBinding
import ke.co.definition.inkopies.presentation.common.SLMDialogFragment
import ke.co.definition.inkopies.presentation.common.selectAllOnFocus
import ke.co.definition.inkopies.presentation.common.showKeyboard
import ke.co.definition.inkopies.presentation.shopping.list.UpsertListItemDialogFrag
import java.util.*

/**
 * Created by tomogoma
 * On 28/05/18.
 */
class CheckoutDialogFrag : SLMDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val vmFactory = (activity!!.application as App).appComponent.provideCheckoutVMFactory()
        val vm = ViewModelProviders.of(this, vmFactory)
                .get(CheckoutVM::class.java)

        val views: DialogCheckoutBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_checkout, container, false)
        views.vm = vm
        views.setLifecycleOwner(this)

        observeViewModel(views)
        observeViews(views)
        start(views)

        return views.root
    }

    private fun start(v: DialogCheckoutBinding) {
        val slID = arguments!!.getString(EXTRA_SHOPPING_LIST_ID)
        v.vm!!.onStart(slID)
        v.storeName.selectAllOnFocus()
        v.branchName.selectAllOnFocus()
        v.storeName.showKeyboard(context!!)
    }

    private fun observeViewModel(v: DialogCheckoutBinding) {
        v.vm!!.snackbarData.observe(this, Observer { it?.show(v.root) })
        v.vm!!.onCompleteEvent.observe(this, Observer { dismiss() })
        observedLiveData.addAll(listOf(v.vm!!.snackbarData, v.vm!!.onCompleteEvent))
    }

    private fun observeViews(v: DialogCheckoutBinding) {
        v.checkoutDateOverlay.setOnClickListener { selectDate(v) }
        v.branchName.setOnEditorActionListener { _, action, _ ->
            if (action != EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener false
            }
            v.vm!!.onSubmit()
            return@setOnEditorActionListener true
        }
        v.submit.setOnClickListener { v.vm!!.onSubmit() }
    }

    private fun selectDate(v: DialogCheckoutBinding) {
        DatePickerFragment().apply {
            date = v.vm!!.date
            onDateSetListener = { newDate -> v.vm!!.onCheckoutDateSet(newDate) }
        }.show(activity!!.supportFragmentManager, "datePicker")
    }

    companion object {
        private const val EXTRA_SHOPPING_LIST_ID = "EXTRA_SHOPPING_LIST_ID"

        fun start(fm: FragmentManager, slID: String) {
            CheckoutDialogFrag().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_SHOPPING_LIST_ID, slID)
                }
            }.show(fm, UpsertListItemDialogFrag::class.java.name)
        }
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        internal var date: Date = Date()
        internal var onDateSetListener: (newDate: Date) -> Unit = {}

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance().apply { time = date }
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(activity!!, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            onDateSetListener(cal.time)
        }
    }
}