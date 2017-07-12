package ke.co.definition.inkopies.views

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentShoppingListPlanBinding
import ke.co.definition.inkopies.model.Model
import ke.co.definition.inkopies.model.beans.ShoppingList
import ke.co.definition.inkopies.model.beans.ShoppingListBrand

/**
 * Created by tomogoma on 09/07/17.
 */
class ShoppingListPlanFragment : Fragment() {

    companion object {

        private val CLASS_NAME = ShoppingListPlanFragment::class.java.name!!
        private val EXTRA_SHOPPING_LIST = CLASS_NAME + "EXTRA_SHOPPING_LIST"

        fun initialize(sl: ShoppingList): ShoppingListPlanFragment {
            val f = ShoppingListPlanFragment()
            val args = Bundle()
            args.putSerializable(EXTRA_SHOPPING_LIST, sl)
            f.arguments = args
            return f
        }
    }

    interface PriceSettable {
        fun setPrice(total_selected: Pair<Float, Float>)
    }

    private lateinit var adapter: ShoppingListBrandAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentShoppingListPlanBinding>(
                inflater, R.layout.fragment_shopping_list_plan, container, false)
        val sl = arguments.getSerializable(EXTRA_SHOPPING_LIST) as ShoppingList
        binding.items.setHasFixedSize(true)
        val lm = LinearLayoutManager(context)
        binding.items.layoutManager = lm
        adapter = ShoppingListBrandAdapter(sl, context)
        val did = DividerItemDecoration(context, lm.orientation)
        binding.items.addItemDecoration(did)
        binding.items.adapter = adapter

        adapter.setOnPriceChangeListener { newPrices ->
            (activity as PriceSettable).setPrice(newPrices)
        }

        loadList(sl)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity !is PriceSettable) {
            throw RuntimeException("Activity needs to implement PriceSettable interface")
        }
    }

    fun newShoppingListBrand() {
        adapter.newShoppingListBrand()
    }

    fun loadList(sl: ShoppingList) {
        Model.getShoppingListBrands(sl.currMode!!, sl.id!!, resultCallback = { res ->
            val r: MutableList<ShoppingListBrand> = res as MutableList
            if (r.isEmpty()) {
                adapter.newShoppingListBrand()
                return@getShoppingListBrands
            }
            adapter.setShoppingListBrands(r)
        })
    }
}
