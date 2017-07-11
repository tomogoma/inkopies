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

        fun shoppingListNameExists(sl: ShoppingList): UUID? {
            return SQLite.select(ShoppingList_Table.localID)
                    .from(ShoppingList::class.java)
                    .where(ShoppingList_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        fun measuringUnitNameExists(sl: MeasuringUnit): UUID? {
            return SQLite.select(MeasuringUnit_Table.localID)
                    .from(MeasuringUnit::class.java)
                    .where(MeasuringUnit_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        fun itemNameExists(sl: Item): UUID? {
            return SQLite.select(Item_Table.localID)
                    .from(Item::class.java)
                    .where(Item_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        fun brandNameForItemExists(sl: Brand): UUID? {
            return SQLite.select(Brand_Table.localID)
                    .from(Brand::class.java)
                    .where(Brand_Table.item_localID.eq(sl.item!!.localID))
                    .and(Brand_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        fun shoppingListBrandForBrandExists(slb: ShoppingListBrand): UUID? {
            return SQLite.select(ShoppingListBrand_Table.localID)
                    .from(ShoppingListBrand::class.java)
                    .where(ShoppingListBrand_Table.brand_localID.eq(slb.brand!!.localID))
                    .or(ShoppingListBrand_Table.localID.eq(slb.localID))
                    .querySingle()
                    ?.localID
        }

        /**
         * newShoppingList synchronously saves @link{ShoppingList} into the db.
         */
        fun newShoppingList(sl: ShoppingList): Boolean {
            if (sl.name.isNullOrBlank()) {
                throw RuntimeException("ShoppingList name was null or blank")
            }
            val localID = shoppingListNameExists(sl)
            if (localID != null) {
                sl.localID = localID
                return false
            }
            return sl.save()
        }

        fun newShoppingListBrand(slb: ShoppingListBrand): Boolean {
            val mu = slb.brand!!.measuringUnit ?: slb.brand!!.item!!.measuringUnit
            if (mu != null) {
                newMeasuringUnit(mu)
            }
            newItem(slb.brand!!.item!!)
            newBrand(slb.brand!!)
            newShoppingList(slb.shoppingList!!)
            val localID = shoppingListBrandForBrandExists(slb)
            if (localID != null) {
                slb.localID = localID
                slb.update()
                return false
            }
            return slb.save()
        }

        /**
         * returns <updateSuccess, hasDeletedAnEntry>
         */
        fun updateShoppingListBrand(slb: ShoppingListBrand): Pair<Boolean, Boolean> {
            val mu = slb.brand!!.measuringUnit ?: slb.brand!!.item!!.measuringUnit
            if (mu != null) {
                newMeasuringUnit(mu)
            }
            newItem(slb.brand!!.item!!)
            newBrand(slb.brand!!)
            newShoppingList(slb.shoppingList!!)
            val localID = shoppingListBrandForBrandExists(slb)
            if (localID != null && localID != slb.localID) {
                if (!slb.delete()) {
                    return Pair(false, false)
                }
                slb.localID = localID
                return Pair(slb.update(), true)
            }
            return Pair(slb.update(), false)
        }

        fun deleteShoppingListBrand(slb: ShoppingListBrand): Boolean {
            if (!slb.exists()) {
                return false
            }
            return slb.delete()
        }

        fun newBrand(br: Brand): Boolean {
            if (br.name == null) {
                br.name = ""
            }
            val localID = brandNameForItemExists(br)
            if (localID != null) {
                br.localID = localID
                br.update()
                return false
            }
            return br.save()
        }

        fun newItem(it: Item): Boolean {
            if (it.name.isNullOrBlank()) {
                throw RuntimeException("Item name was null or blank")
            }
            val localID = itemNameExists(it)
            if (localID != null) {
                it.localID = localID
                return false
            }
            return it.save()
        }

        fun newMeasuringUnit(mu: MeasuringUnit): Boolean {
            if (mu.name == null) {
                mu.name = ""
            }
            val localID = measuringUnitNameExists(mu)
            if (localID != null) {
                mu.localID = localID
                return false
            }
            return mu.save()
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
        SQLite.select()
                .from(ShoppingListBrand::class.java)
                .where(where)
                .orderBy(ShoppingListBrand_Table.status, true)
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
        contentObserver.registerForContentChanges(ctx, ShoppingListBrand::class.java)
        contentObserver.addOnTableChangedListener { _, _ ->
            getShoppingListBrands(ctx, shoppingListID, resultCallback)
        }
    }

    fun destroy(ctx: Context) {
        contentObserver.unregisterForContentChanges(ctx)
    }
}
