package ke.co.definition.inkopies.views

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.LayoutShoppingListBrandItemBinding
import ke.co.definition.inkopies.databinding.ShoppingListBrandEditBinding
import ke.co.definition.inkopies.model.Model
import ke.co.definition.inkopies.model.beans.ShoppingList
import ke.co.definition.inkopies.model.beans.ShoppingListBrand
import java.util.*


/**
 * Created by tomogoma on 08/07/17.
 */

class ShoppingListBrandAdapter(private var sl: ShoppingList, private var context: Context)
    : RecyclerView.Adapter<ShoppingListBrandAdapter.ViewHolder>() {

    companion object {

        private val STATE_VIEW: Int = 1
        private val STATE_NEW: Int = 2
        private val STATE_EDIT: Int = 3

        private val DEFAULT_FOCUS_VIEW = R.id.itemName
    }

    class ViewHolder(var binding: LayoutShoppingListBrandItemBinding) : RecyclerView.ViewHolder(binding.root)

    private class SLBMapper(var state: Int, var slb: ShoppingListBrand, var focusView: Int)

    private val slbMappers: MutableList<SLBMapper> = LinkedList()

    fun setShoppingListBrands(brands: List<ShoppingListBrand>) {
        slbMappers.clear()
        for (brand in brands) {
            this.slbMappers.add(SLBMapper(STATE_VIEW, brand, DEFAULT_FOCUS_VIEW))
        }
        notifyDataSetChanged()
    }

    fun newShoppingListBrand() {
        val slb = ShoppingListBrand(sl)
        val slbM = SLBMapper(STATE_NEW, slb, DEFAULT_FOCUS_VIEW)
        slbMappers.add(0, slbM)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListBrandAdapter.ViewHolder {
        val binding = DataBindingUtil.inflate<LayoutShoppingListBrandItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.layout_shopping_list_brand_item,
                parent, false
        )
        val vh = ViewHolder(binding)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slbM = slbMappers[position]
        slbM.slb.shoppingList = sl
        slbM.slb.brand!!.load()
        slbM.slb.brand!!.item!!.load()
        (slbM.slb.brand!!.measuringUnit ?: slbM.slb.brand!!.item!!.measuringUnit)?.load()
        holder.binding.slBrand = slbM.slb
        when (slbM.state) {
            STATE_NEW -> bindNewSLB(slbM, holder.binding)
            STATE_EDIT -> bindEditSLB(slbM, holder.binding)
            STATE_VIEW -> bindViewSLB(slbM, holder.binding)
        }
    }

    override fun getItemCount(): Int {
        return slbMappers.size
    }

    private fun bindEditSLB(slbM: SLBMapper, binding: LayoutShoppingListBrandItemBinding) {
        binding.edit.delete.setText(R.string.delete)
        binding.edit.submit.setText(R.string.done)
        showEditView(slbM.focusView, binding)
        highlightAllOnFocus(binding.edit)

        binding.edit.submit.setOnClickListener { _ ->
            hideKeyboard(binding.edit.submit)
            updateShoppingListBrand(slbM, getPosition(slbM))
        }
        binding.edit.delete.setOnClickListener { _ ->
            hideKeyboard(binding.edit.submit)
            Model.deleteShoppingListBrand(binding.slBrand)
            val position = slbMappers.indices.first { slbMappers[it].slb.id == slbM.slb.id }
            slbMappers.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun bindNewSLB(slbM: SLBMapper, binding: LayoutShoppingListBrandItemBinding) {
        binding.edit.delete.setText(R.string.cancel)
        binding.edit.submit.setText(R.string.add)
        showEditView(slbM.focusView, binding)
        highlightAllOnFocus(binding.edit)

        binding.edit.submit.setOnClickListener { _ ->
            hideKeyboard(binding.edit.submit)
            val position = getPosition(slbM)
            if (!validateShoppingListBrand(binding.slBrand)) {
                slbMappers.removeAt(position)
                notifyItemRemoved(position)
                return@setOnClickListener
            }
            if (Model.newShoppingListBrand(binding.slBrand)) {
                slbM.state = STATE_VIEW
                notifyItemChanged(position)
            } else {
                slbMappers.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        binding.edit.delete.setOnClickListener { _ ->
            hideKeyboard(binding.edit.submit)
            val position = getPosition(slbM)
            slbMappers.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun bindViewSLB(slbM: SLBMapper, binding: LayoutShoppingListBrandItemBinding) {
        showViewView(binding)
        binding.view.checkbox.setOnClickListener { _ ->
            val position = getPosition(slbM)
            updateShoppingListBrand(slbM, position)
            slbMappers.removeAt(position)
            notifyItemRemoved(position)
            val p = slbMappers.indices.firstOrNull { !slbMappers[it].slb.isStatusBoxChecked() }
                    ?: slbMappers.size
            slbMappers.add(p, slbM)
            notifyItemInserted(p)
        }
        binding.view.brandName.setOnClickListener { v -> editableClicked(slbM, getPosition(slbM), v) }
        binding.view.itemName.setOnClickListener { v -> editableClicked(slbM, getPosition(slbM), v) }
        binding.view.quantity.setOnClickListener { v -> editableClicked(slbM, getPosition(slbM), v) }
        binding.view.measuringUnit.setOnClickListener { v -> editableClicked(slbM, getPosition(slbM), v) }
        binding.view.unitPrice.setOnClickListener { v -> editableClicked(slbM, getPosition(slbM), v) }
    }

    private fun hideKeyboard(v: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun showKeyboard(v: TextView) {
        v.isFocusable = true
        v.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, 0)
    }

    private fun getPosition(slbM: SLBMapper) = slbMappers.indices.first {
        slbMappers[it].slb.localID == slbM.slb.localID
    }

    private fun editableClicked(slbM: SLBMapper, position: Int, v: View) {
        slbM.state = STATE_EDIT
        slbM.focusView = v.id
        notifyItemChanged(position)
    }

    private fun showEditView(focusView: Int, binding: LayoutShoppingListBrandItemBinding) {
        binding.view.root.visibility = View.GONE
        binding.edit.root.visibility = View.VISIBLE
        val view = when (focusView) {
            R.id.brandName -> binding.edit.brandName
            R.id.quantity -> binding.edit.quantity
            R.id.measuringUnit -> binding.edit.measuringUnit
            R.id.unitPrice -> binding.edit.unitPrice
            R.id.itemName -> binding.edit.itemName
            else -> binding.edit.itemName
        }
        showKeyboard(view)
    }

    private fun showViewView(binding: LayoutShoppingListBrandItemBinding) {
        binding.edit.root.visibility = View.GONE
        binding.view.root.visibility = View.VISIBLE
    }

    private fun highlightAllOnFocus(binding: ShoppingListBrandEditBinding) {
        binding.brandName.setSelectAllOnFocus(true)
        binding.itemName.setSelectAllOnFocus(true)
        binding.quantity.setSelectAllOnFocus(true)
        binding.measuringUnit.setSelectAllOnFocus(true)
        binding.unitPrice.setSelectAllOnFocus(true)
    }

    private fun updateShoppingListBrand(slbM: SLBMapper, position: Int) {
        if (!validateShoppingListBrand(slbM.slb)) {
            slbM.state = STATE_VIEW
            return
        }
        val successDeleted = Model.updateShoppingListBrand(slbM.slb)
        slbM.state = STATE_VIEW
        if (!successDeleted.first) {
            // TODO
        }
        if (successDeleted.second) {
            slbMappers.removeAt(position)
            notifyItemRemoved(position)
            return
        }
        notifyItemChanged(position)
    }

    private fun validateShoppingListBrand(slb: ShoppingListBrand): Boolean {
        return !slb.brand!!.item!!.name.isNullOrBlank() && slb.quantity!! > 0
    }
}
