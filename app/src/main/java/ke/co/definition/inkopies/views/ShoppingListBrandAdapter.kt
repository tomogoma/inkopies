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
        holder.binding.edit.submit.setOnClickListener { view ->
            if (validateShoppingListBrand(holder.binding.slBrand)) {
                shoppingListBrands.removeAt(position)
                notifyItemRemoved(position)
                return@setOnClickListener
            }
            if (Model.newShoppingListBrand(holder.binding.slBrand) || slbv.state == STATE_EDIT) {
                slbv.state = STATE_VIEW
                notifyItemChanged(position)
            } else {
                shoppingListBrands.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        holder.binding.edit.delete.setOnClickListener { view ->
            if (slbv.state == STATE_EDIT) {
                Model.deleteShoppingListBrand(holder.binding.slBrand)
            }
            shoppingListBrands.removeAt(position)
            notifyItemRemoved(position)
        }
        holder.binding.slBrand = slbv.slb
        holder.binding.slBrand.brand!!.load()
        holder.binding.slBrand.brand!!.item!!.load()
        (holder.binding.slBrand.brand!!.measuringUnit ?:
                holder.binding.slBrand.brand!!.item!!.measuringUnit)?.load()
        when (slbv.state) {
            STATE_NEW -> {
                holder.binding.edit.delete.setText(R.string.cancel)
                holder.binding.edit.submit.setText(R.string.add)
                holder.binding.view.root.visibility = View.GONE
                holder.binding.edit.root.visibility = View.VISIBLE
            }
            STATE_EDIT -> {
                holder.binding.edit.delete.setText(R.string.delete)
                holder.binding.edit.submit.setText(R.string.done)
                holder.binding.view.root.visibility = View.GONE
                holder.binding.edit.root.visibility = View.VISIBLE
            }
            else -> {
                holder.binding.edit.delete.setText(R.string.delete)
                holder.binding.edit.submit.setText(R.string.done)
                holder.binding.edit.root.visibility = View.GONE
                holder.binding.view.root.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return shoppingListBrands.size
    }

    private fun validateShoppingListBrand(slb: ShoppingListBrand): Boolean {
        return slb.brand!!.item!!.name.isNullOrBlank() || slb.quantity!! < 0
    }
}
