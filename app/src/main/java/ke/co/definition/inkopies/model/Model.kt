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
         * upsertShoppingList synchronously saves @link{ShoppingList} into the db.
         */
        fun upsertShoppingList(sl: ShoppingList) {
            if (sl.name.isNullOrBlank()) {
                throw RuntimeException("ShoppingList name was null or blank")
            }
            val localID = shoppingListNameExists(sl)
            val res: Boolean
            if (localID != null) {
                sl.localID = localID
                res = sl.update()
            } else {
                res = sl.save()
            }
            if (!res) {
                throw RuntimeException("Unable to upsert ShoppingList")
            }
        }

        fun insertShoppingListBrand(slb: ShoppingListBrand) {
            upsertBrand(slb.brand!!)
            upsertShoppingList(slb.shoppingList!!)
            val localID = shoppingListBrandForBrandExists(slb)
            val res: Boolean
            if (localID != null) {
                slb.localID = localID
                res = slb.update()
            } else {
                res = slb.save()
            }
            if (!res) {
                throw RuntimeException("Unable to insert ShoppingListBrand")
            }
        }

        /**
         * returns <updateSuccess, hasDeletedAnEntry>
         */
        fun updateShoppingListBrand(slb: ShoppingListBrand): Boolean {
            upsertBrand(slb.brand!!)
            upsertShoppingList(slb.shoppingList!!)
            val localID = shoppingListBrandForBrandExists(slb)
            val res: Boolean
            var hadDeleted = false
            if (localID != null && localID != slb.localID) {
                if (!slb.delete()) {
                    throw RuntimeException("Unable to delete (found existing" +
                            " with name, tried to delete current in order to " +
                            "update current instead)")
                }
                hadDeleted = true
                slb.localID = localID
                res = slb.update()
            } else {
                res = slb.update()
            }
            if (!res) {
                throw RuntimeException("Unable to update ShoppingListBrand")
            }
            return hadDeleted
        }

        fun deleteShoppingListBrand(slb: ShoppingListBrand) {
            if (!slb.exists()) {
                return
            }
            if (!slb.delete()) {
                throw RuntimeException("Unable to delete ShoppingListBrand")
            }
        }

        fun upsertBrand(br: Brand) {
            if (br.name == null) {
                br.name = ""
            }
            newItem(br.item!!)
            if (br.measuringUnit != null) {
                newMeasuringUnit(br.measuringUnit!!)
            }
            val localID = brandNameForItemExists(br)
            val res: Boolean
            if (localID != null) {
                br.localID = localID
                res = br.update()
            } else {
                res = br.save()
            }
            if (!res) {
                throw RuntimeException("Unable to upsert Brand")
            }
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

        fun getShoppingListBrands(mode: ShoppingList.Mode, shoppingListID: UUID, resultCallback: (res: List<ShoppingListBrand>) -> Unit) {
            val where = when (mode) {
                ShoppingList.Mode.SHOPPING -> arrayOf(
                        ShoppingListBrand_Table.shoppingList_localID.eq(shoppingListID),
                        ShoppingListBrand_Table.status.greaterThanOrEq(ShoppingListBrand.STATUS_SCHEDULED)
                )
                else -> arrayOf(
                        ShoppingListBrand_Table.shoppingList_localID.eq(shoppingListID)
                )
            }
            SQLite.select()
                    .from(ShoppingListBrand::class.java)
                    .where(*where)
                    .orderBy(ShoppingListBrand_Table.status, false)
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

        private fun newItem(it: Item) {
            if (it.name.isNullOrBlank()) {
                throw RuntimeException("Item name was null or blank")
            }
            if (it.measuringUnit != null) {
                newMeasuringUnit(it.measuringUnit!!)
            }
            val localID = itemNameExists(it)
            if (localID != null) {
                it.localID = localID
                return
            }
            if (!it.save()) {
                throw RuntimeException("Unable to save new Item")
            }
        }

        private fun newMeasuringUnit(mu: MeasuringUnit) {
            if (mu.name == null) {
                mu.name = ""
            }
            val localID = measuringUnitNameExists(mu)
            if (localID != null) {
                mu.localID = localID
                return
            }
            if (!mu.save()) {
                throw RuntimeException("Unable to save new MeasuringUnit")
            }
        }

        private fun shoppingListNameExists(sl: ShoppingList): UUID? {
            return SQLite.select(ShoppingList_Table.localID)
                    .from(ShoppingList::class.java)
                    .where(ShoppingList_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        private fun measuringUnitNameExists(sl: MeasuringUnit): UUID? {
            return SQLite.select(MeasuringUnit_Table.localID)
                    .from(MeasuringUnit::class.java)
                    .where(MeasuringUnit_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        private fun itemNameExists(sl: Item): UUID? {
            return SQLite.select(Item_Table.localID)
                    .from(Item::class.java)
                    .where(Item_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        private fun brandNameForItemExists(sl: Brand): UUID? {
            return SQLite.select(Brand_Table.localID)
                    .from(Brand::class.java)
                    .where(Brand_Table.item_localID.eq(sl.item!!.localID))
                    .and(Brand_Table.name.eq(sl.name))
                    .querySingle()
                    ?.localID
        }

        private fun shoppingListBrandForBrandExists(slb: ShoppingListBrand): UUID? {
            return SQLite.select(ShoppingListBrand_Table.localID)
                    .from(ShoppingListBrand::class.java)
                    .where(ShoppingListBrand_Table.brand_localID.eq(slb.brand!!.localID))
                    .or(ShoppingListBrand_Table.localID.eq(slb.localID))
                    .querySingle()
                    ?.localID
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

    fun destroy(ctx: Context) {
        contentObserver.unregisterForContentChanges(ctx)
    }
}
