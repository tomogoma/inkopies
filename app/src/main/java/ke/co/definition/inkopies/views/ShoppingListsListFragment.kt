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
import ke.co.definition.inkopies.databinding.FragmentShoppingListsListBinding
import ke.co.definition.inkopies.model.Model
import ke.co.definition.inkopies.model.beans.ShoppingList


class ShoppingListsListFragment : Fragment() {

    companion object {
        fun instantiate() = ShoppingListsListFragment()
    }

    private val model: Model = Model()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentShoppingListsListBinding>(
                inflater, R.layout.fragment_shopping_lists_list, container, false)
        binding.shoppingLists.setHasFixedSize(true)
        val lm = LinearLayoutManager(context)
        binding.shoppingLists.layoutManager = lm
        val adapter = ShoppingListsListAdapter(activity, null)
        binding.shoppingLists.adapter = adapter
        val did = DividerItemDecoration(context, lm.orientation)
        binding.shoppingLists.addItemDecoration(did)

        model.getProfiles(context, ShoppingList::class.java, resultCallback = { sls ->
            kotlin.run {
                if (sls.isEmpty()) {
                    NewShoppingListDialogActivity.startForReason(activity, R.string.first_shopping_list)
                    return@run
                }
                adapter.setShoppingLists(sls)
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        model.destroy(context)
        super.onDestroy()
    }

}
