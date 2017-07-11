package ke.co.definition.inkopies.views

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.LayoutShoppingListBrandItemBinding
import ke.co.definition.inkopies.model.Model
import ke.co.definition.inkopies.model.beans.ShoppingList
import ke.co.definition.inkopies.model.beans.ShoppingListBrand
import java.util.*

/**
 * Created by tomogoma on 08/07/17.
 */

class ShoppingListBrandAdapter(private var sl: ShoppingList)
    : RecyclerView.Adapter<ShoppingListBrandAdapter.ViewHolder>() {

    companion object {

        private val STATE_VIEW: Int = 1
        private val STATE_NEW: Int = 2
        private val STATE_EDIT: Int = 3
    }

    class ViewHolder(var binding: LayoutShoppingListBrandItemBinding) : RecyclerView.ViewHolder(binding.root)

    private class ShoppingListBrandView(var state: Int, var slb: ShoppingListBrand)

    private val shoppingListBrands: MutableList<ShoppingListBrandView> = LinkedList()

    fun setShoppingListBrands(brands: List<ShoppingListBrand>) {
        shoppingListBrands.clear()
        for (brand in brands) {
            this.shoppingListBrands.add(ShoppingListBrandView(STATE_VIEW, brand))
        }
        notifyDataSetChanged()
    }

    fun newShoppingListBrand() {
        val slb = ShoppingListBrand(sl)
        val slbv = ShoppingListBrandView(STATE_NEW, slb)
        shoppingListBrands.add(0, slbv)
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
        val slbv = shoppingListBrands[position]
        slbv.slb.shoppingList = sl
        slbv.slb.brand!!.load()
        slbv.slb.brand!!.item!!.load()
        (slbv.slb.brand!!.measuringUnit ?: slbv.slb.brand!!.item!!.measuringUnit)?.load()
        holder.binding.slBrand = slbv.slb
        when (slbv.state) {
            STATE_NEW -> bindNewSLB(slbv, holder.binding, position)
            STATE_EDIT -> bindEditSLB(slbv, holder.binding, position)
            STATE_VIEW -> bindViewSLB(slbv, holder.binding, position)
        }
    }

    override fun getItemCount(): Int {
        return shoppingListBrands.size
    }

    private fun bindEditSLB(slbv: ShoppingListBrandView, binding: LayoutShoppingListBrandItemBinding, position: Int) {
        binding.edit.delete.setText(R.string.delete)
        binding.edit.submit.setText(R.string.done)
        showEditView(binding)

        binding.edit.submit.setOnClickListener { _ ->
            updateShoppingListBrand(slbv, position)
        }
        binding.edit.delete.setOnClickListener { _ ->
            Model.deleteShoppingListBrand(binding.slBrand)
            shoppingListBrands.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun bindNewSLB(slbv: ShoppingListBrandView, binding: LayoutShoppingListBrandItemBinding, position: Int) {
        binding.edit.delete.setText(R.string.cancel)
        binding.edit.submit.setText(R.string.add)
        showEditView(binding)

        binding.edit.submit.setOnClickListener { _ ->
            if (!validateShoppingListBrand(binding.slBrand)) {
                shoppingListBrands.removeAt(position)
                notifyItemRemoved(position)
                return@setOnClickListener
            }
            if (Model.newShoppingListBrand(binding.slBrand)) {
                slbv.state = STATE_VIEW
                notifyItemChanged(position)
            } else {
                shoppingListBrands.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        binding.edit.delete.setOnClickListener { _ ->
            shoppingListBrands.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun bindViewSLB(slbv: ShoppingListBrandView, binding: LayoutShoppingListBrandItemBinding, position: Int) {
        showViewView(binding)
        binding.view.checkbox.setOnCheckedChangeListener { _, checked ->
            updateShoppingListBrand(slbv, position)
        }
    }

    private fun showEditView(binding: LayoutShoppingListBrandItemBinding) {
        binding.view.root.visibility = View.GONE
        binding.edit.root.visibility = View.VISIBLE
    }

    private fun showViewView(binding: LayoutShoppingListBrandItemBinding) {
        binding.edit.root.visibility = View.GONE
        binding.view.root.visibility = View.VISIBLE
    }

    private fun updateShoppingListBrandOnCheckChanged(checked: Boolean, slbv: ShoppingListBrandView, position: Int) {
        shoppingListBrands.removeAt(position)
        var newPos = shoppingListBrands.size
        if (checked) {
            shoppingListBrands.add(0, slbv)
            newPos = 0
        } else {
            shoppingListBrands.add(slbv)
        }
        notifyItemChanged(newPos)
    }

    private fun updateShoppingListBrand(slbv: ShoppingListBrandView, position: Int) {
        if (!validateShoppingListBrand(slbv.slb)) {
            slbv.state = STATE_VIEW
            return
        }
        val successDeleted = Model.updateShoppingListBrand(slbv.slb)
        slbv.state = STATE_VIEW
        if (!successDeleted.first) {
            // TODO
        }
        if (successDeleted.second) {
            shoppingListBrands.removeAt(position)
            notifyItemRemoved(position)
            return
        }
        notifyItemChanged(position)
    }

    private fun validateShoppingListBrand(slb: ShoppingListBrand): Boolean {
        return !slb.brand!!.item!!.name.isNullOrBlank() && slb.quantity!! > 0
    }
}
