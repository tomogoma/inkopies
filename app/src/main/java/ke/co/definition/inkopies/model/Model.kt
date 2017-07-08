package ke.co.definition.inkopies.model

import android.content.Context
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.language.SQLite
import ke.co.definition.inkopies.model.beans.Profile
import ke.co.definition.inkopies.model.beans.ShoppingList

/**
 * Created by tomogoma on 08/07/17.
 */

class Model {

    companion object {
        fun init(c: Context) {
            FlowManager.init(c)
        }

        fun newShoppingList(sl: ShoppingList) = sl.save()
    }

    val contentObserver: FlowContentObserver = FlowContentObserver()

    fun <T : Profile> getProfiles(ctx: Context, cls: Class<T>, resultCallback: (res: List<T>) -> Unit) {
        getProfiles(cls, resultCallback)
        contentObserver.registerForContentChanges(ctx, cls)
        contentObserver.addOnTableChangedListener { _, _ -> getProfiles(cls, resultCallback) }
    }

    fun destroy(ctx: Context) {
        contentObserver.unregisterForContentChanges(ctx)
    }

    private fun <T : Profile> getProfiles(cls: Class<T>, resultCallback: (res: List<T>) -> Unit) {
        SQLite.select()
                .from(cls)
                .async()
                .queryResultCallback { _, res ->
                    run {
                        val sls = res.toList()
                        resultCallback(sls)
                    }
                }
                .error { _, error ->
                    throw RuntimeException(error)
                }
                .execute()
    }
}
