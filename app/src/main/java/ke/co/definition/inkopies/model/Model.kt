package ke.co.definition.inkopies.model

import android.content.Context
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.language.SQLOperator
import com.raizlabs.android.dbflow.sql.language.SQLite
import ke.co.definition.inkopies.model.beans.*
import java.util.*

/**
 * Created by tomogoma on 08/07/17.
 */

class Model {

    companion object {
        fun init(c: Context) {
            FlowManager.init(c)
        }

        /**
         * shoppingListExists asynchronously checks and returns true if the
         * @link{ShoppingList.name} exists.
         */
        fun shoppingListNameExists(sl: ShoppingList, hasShoppingListCallBack: (has: Boolean) -> Unit) {
            SQLite.select(ShoppingList_Table.localID)
                    .from(ShoppingList::class.java)
                    .where(ShoppingList_Table.name.eq(sl.name))
                    .async()
                    .queryResultCallback({ _, res ->
                        res.use { r ->
                            val sls = r.toList()
                            hasShoppingListCallBack(!sls.isEmpty())
                        }
                    })
                    .error { _, error -> throw RuntimeException(error) }
                    .execute()
        }

        /**
         * newShoppingList synchronously saves @link{ShoppingList} into the db.
         */
        fun newShoppingList(sl: ShoppingList): Boolean {
            return sl.save()
        }

        /**
         * getProfiles asynchronously fetches {@link Profile}s from the db.
         */
        fun <T : Profile> getProfiles(cls: Class<T>, vararg where: SQLOperator, resultCallback: (res: List<T>) -> Unit) {
            SQLite.select()
                    .from(cls)
                    .where(*where)
                    .async()
                    .queryResultCallback { _, res ->
                        run {
                            res.use { r ->
                                val sls = r.toList()
                                resultCallback(sls)
                            }
                        }
                    }
                    .error { _, error ->
                        throw RuntimeException(error)
                    }
                    .execute()
        }
    }

    val contentObserver: FlowContentObserver = FlowContentObserver()


    /**
     * getProfiles asynchronously fetches {@link Profile}s and holds a db observer
     * to re-fetch on db-update. You MUST call destroy whenever your context is destroyed
     * after calling this method.
     */
    fun <T : Profile> getProfiles(ctx: Context, cls: Class<T>, vararg where: SQLOperator, resultCallback: (res: List<T>) -> Unit) {
        getProfiles(cls, *where, resultCallback = resultCallback)
        contentObserver.registerForContentChanges(ctx, cls)
        contentObserver.addOnTableChangedListener { _, _ -> getProfiles(cls, *where, resultCallback = resultCallback) }
    }

    fun getShoppingListBrands(ctx: Context, shoppingListID: UUID, resultCallback: (res: List<ShoppingListBrand>) -> Unit) {
        val where = ShoppingListBrand_Table.shoppingList_localID.eq(shoppingListID)
        getProfiles(ctx, ShoppingListBrand::class.java, where, resultCallback = resultCallback)
    }

    fun destroy(ctx: Context) {
        contentObserver.unregisterForContentChanges(ctx)
    }
}
