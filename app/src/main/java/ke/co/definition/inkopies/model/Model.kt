package ke.co.definition.inkopies.model

import android.content.Context
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.language.SQLOperator
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.property.Property
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
            val existing = nameableExists(ShoppingList_Table.name, sl)
            if (existing != null) {
                sl.inheritIdentification(existing)
                sl.update()
                return
            }
            if (!sl.save()) {
                throw RuntimeException("Unable to save ShoppingListBrand")
            }
        }

        fun upsertShoppingListBrand(slb: ShoppingListBrand): Boolean {
            upsertBrand(slb.brand!!)
            upsertShoppingList(slb.shoppingList!!)
            val existing = shoppingListBrandForBrandExists(slb)
            if (existing != null) {
                slb.inheritIdentification(existing)
                slb.update()
                return false
            }
            if (!slb.save()) {
                throw RuntimeException("Unable to save ShoppingListBrand")
            }
            return true
        }

        fun updateShoppingListBrand(slb: ShoppingListBrand): Boolean {
            upsertBrand(slb.brand!!)
            upsertShoppingList(slb.shoppingList!!)
            val existing = shoppingListBrandForBrandExists(slb)
            if (existing != null && existing.id != slb.id) {
                if (!slb.delete()) {
                    throw RuntimeException("Unable to delete (found existing" +
                            " with name, tried to delete current in order to " +
                            "update current instead)")
                }
                slb.inheritIdentification(existing)
                slb.update()
                return true
            }
            slb.updateDate = Date()
            slb.update()
            return false
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
            val existing = brandNameForItemExists(br)
            if (existing != null) {
                br.inheritIdentification(existing)
                br.update()
                return
            }
            if (!br.save()) {
                throw RuntimeException("Unable to save Brand")
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

        fun getShoppingListBrands(slMode: Int, shoppingListID: UUID, resultCallback: (res: List<ShoppingListBrand>) -> Unit) {
            val where = when (slMode) {
                ShoppingList.SHOPPING -> arrayOf(
                        ShoppingListBrand_Table.shoppingList_id.eq(shoppingListID),
                        ShoppingListBrand_Table.status.greaterThanOrEq(ShoppingListBrand.STATUS_SCHEDULED)
                )
                else -> arrayOf(
                        ShoppingListBrand_Table.shoppingList_id.eq(shoppingListID)
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

        private fun newItem(it: Item) {
            if (it.name.isNullOrBlank()) {
                throw RuntimeException("Item name was null or blank")
            }
            if (it.measuringUnit != null) {
                newMeasuringUnit(it.measuringUnit!!)
            }
            val existing = nameableExists(Item_Table.name, it)
            if (existing != null) {
                it.inheritIdentification(existing)
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
            val existing = nameableExists(MeasuringUnit_Table.name, mu)
            if (existing != null) {
                mu.inheritIdentification(existing)
                return
            }
            if (!mu.save()) {
                throw RuntimeException("Unable to save new MeasuringUnit")
            }
        }

        private fun nameableExists(nameCol: Property<String>, nm: Nameable): Profile? {
            return SQLite.select()
                    .from(nm.javaClass)
                    .where(nameCol.eq(nm.name))
                    .querySingle()
        }

        private fun brandNameForItemExists(sl: Brand): Profile? {
            return SQLite.select()
                    .from(Brand::class.java)
                    .where(Brand_Table.item_id.eq(sl.item!!.id))
                    .and(Brand_Table.name.eq(sl.name))
                    .querySingle()
        }

        private fun shoppingListBrandForBrandExists(slb: ShoppingListBrand): Profile? {
            return SQLite.select()
                    .from(ShoppingListBrand::class.java)
                    .where(ShoppingListBrand_Table.brand_id.eq(slb.brand!!.id))
                    .or(ShoppingListBrand_Table.id.eq(slb.id))
                    .querySingle()
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
