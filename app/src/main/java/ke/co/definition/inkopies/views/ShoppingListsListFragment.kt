package ke.co.definition.inkopies.views

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raizlabs.android.dbflow.sql.language.SQLite
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.FragmentShoppingListsListBinding
import ke.co.definition.inkopies.model.beans.ShoppingList

class ShoppingListsListFragment : Fragment() {

    companion object {
        fun instantiate() = ShoppingListsListFragment()
    }

    val logTag: String = ShoppingListsListFragment::class.java.name
    var binding: FragmentShoppingListsListBinding? = null
    var adapter: ShoppingListsListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_shopping_lists_list, container, false)
        binding?.shoppingLists?.setHasFixedSize(true)
        val lm = LinearLayoutManager(context)
        binding?.shoppingLists?.layoutManager = lm
        adapter = ShoppingListsListAdapter(null)
        binding?.shoppingLists?.adapter = adapter
        fetchData()
        return binding?.root
    }

    fun fetchData() {
        SQLite.select()
                .from(ShoppingList::class.java)
                .async()
                .queryResultCallback { _, res ->
                    run {
                        val sls = res.toList()
                        if (sls.isEmpty()) {
                            NewShoppingListDialogActivity.start(activity)
                            return@run
                        }
                        adapter?.setShoppingLists(sls)
                    }
                }
                .error { _, error ->
                    run {
                        Log.d(logTag, "got error $error")
                    }
                }
                .execute()
    }

}
