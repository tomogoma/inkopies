package ke.co.definition.inkopies.model

import android.content.Context
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.language.SQLite
import ke.co.definition.inkopies.model.beans.Profile
import ke.co.definition.inkopies.model.beans.ShoppingList
import ke.co.definition.inkopies.model.beans.ShoppingList_Table

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
        fun <T : Profile> getProfiles(cls: Class<T>, resultCallback: (res: List<T>) -> Unit) {
            SQLite.select()
                    .from(cls)
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
    fun <T : Profile> getProfiles(ctx: Context, cls: Class<T>, resultCallback: (res: List<T>) -> Unit) {
        getProfiles(cls, resultCallback)
        contentObserver.registerForContentChanges(ctx, cls)
        contentObserver.addOnTableChangedListener { _, _ -> getProfiles(cls, resultCallback) }
    }

    fun destroy(ctx: Context) {
        contentObserver.unregisterForContentChanges(ctx)
    }
}
