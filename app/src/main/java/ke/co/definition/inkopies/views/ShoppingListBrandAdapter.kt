package ke.co.definition.inkopies.views

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.LayoutShoppingListBrandItemBinding
import ke.co.definition.inkopies.model.beans.ShoppingListBrand

/**
 * Created by tomogoma on 08/07/17.
 */

class ShoppingListBrandAdapter(private var brands: List<ShoppingListBrand>?)
    : RecyclerView.Adapter<ShoppingListBrandAdapter.ViewHolder>() {

    class ViewHolder(var binding: LayoutShoppingListBrandItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun setShoppingListBrandss(brands: List<ShoppingListBrand>) {
        this.brands = brands
        notifyDataSetChanged()
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
        holder.binding.slBrand = brands!![position]
        if (holder.binding.slBrand.brand?.name.isNullOrBlank()) {
            holder.binding.view.root.visibility = View.GONE
            holder.binding.edit.root.visibility = View.VISIBLE
        } else {
            holder.binding.edit.root.visibility = View.GONE
            holder.binding.view.root.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return brands?.size ?: 0
    }
}
