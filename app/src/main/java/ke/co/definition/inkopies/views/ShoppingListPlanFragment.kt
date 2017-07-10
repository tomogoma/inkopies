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
import ke.co.definition.inkopies.model.beans.ShoppingListBrand
import java.util.*

/**
 * Created by tomogoma on 09/07/17.
 */
class ShoppingListPlanFragment : Fragment() {

    companion object {

        val CLASS_NAME = ShoppingListPlanFragment::class.java.name!!
        val LOG_TAG = CLASS_NAME
        val EXTRA_SHOPPING_LIST_ID = CLASS_NAME + "EXTRA_SHOPPING_LIST_ID"

        fun initialize(shoppingListID: UUID): ShoppingListPlanFragment {
            val f = ShoppingListPlanFragment()
            val args = Bundle()
            args.putSerializable(EXTRA_SHOPPING_LIST_ID, shoppingListID)
            f.arguments = args
            return f
        }
    }

    var model: Model? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = Model()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentShoppingListPlanBinding>(
                inflater, R.layout.fragment_shopping_list_plan, container, false)
        val adapter = ShoppingListBrandAdapter(null)
        binding.items.adapter = adapter
        val lm = LinearLayoutManager(context)
        binding.items.layoutManager = lm
        val did = DividerItemDecoration(context, lm.orientation)
        binding.items.addItemDecoration(did)

        val shoppingLIstID: UUID = arguments.getSerializable(EXTRA_SHOPPING_LIST_ID) as UUID
        model!!.getShoppingListBrands(context, shoppingLIstID, resultCallback = { res ->
            val r: MutableList<ShoppingListBrand> = res as MutableList
            if (r.isEmpty()) {
                r.add(ShoppingListBrand())
            }
            adapter.setShoppingListBrandss(r)
        })
        return binding.root
    }

    override fun onDestroy() {
        model!!.destroy(context)
        super.onDestroy()
    }
}
