package ke.co.definition.inkopies.model.backup

import android.net.Uri
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ExternalStorageUnavailableException
import ke.co.definition.inkopies.model.FileHelper
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.utils.logging.Logger
import org.supercsv.cellprocessor.ift.CellProcessor
import org.supercsv.io.CsvBeanReader
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import rx.Completable
import rx.Observable
import rx.Single
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 09/04/18.
 */
interface Exporter {
    fun exportShoppingLists(): Completable
    fun exportShoppingList(list: ShoppingList): Completable
    fun importLists(uri: Uri): Completable
}

class ExporterImpl @Inject constructor(
        private val shopping: ShoppingManager,
        private val files: FileHelper,
        private val logger: Logger,
        private val auth: Authable,
        private val resMan: ResourceManager
) : Exporter {

    override fun exportShoppingList(list: ShoppingList): Completable = Completable.create {
        shopping.getShoppingListItems(ShoppingListItemsFilter(list.id), 0, 1000)
                .map { it.map { ExporterShoppingListItem(list.name, it) } }
                .subscribe({ items ->
                    shoppingListItemsToCSV(list.name, items)
                    it.onCompleted()
                }, { ex -> it.onError(ex) })
    }

    override fun exportShoppingLists(): Completable = Completable.create {

        val items = mutableListOf<ExporterShoppingListItem>()

        shopping.getShoppingLists(0, 100)
                .first()
                .toSingle()
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "get first shopping lists"))
                }

                .toObservable()
                .flatMap { lists ->

                    var rsltObsvbl: Observable<List<ExporterShoppingListItem>>? = null

                    lists.forEachIndexed { i, list ->

                        val filter = ShoppingListItemsFilter(list.id)
                        val currObsvbl = shopping.getShoppingListItems(filter, 0, 1000)
                                .map {
                                    it.map { ExporterShoppingListItem(list.name, it) }
                                }
                                .toObservable()
                                .onErrorResumeNext {
                                    Observable.error(handleAuthErrors(logger, auth, resMan, it,
                                            "get shopping list items for ${list.name} -> ${list.id}"))
                                }

                        if (i == 0) {
                            rsltObsvbl = currObsvbl
                            return@forEachIndexed
                        }

                        rsltObsvbl = Observable.concat(rsltObsvbl, currObsvbl)
                    }

                    return@flatMap rsltObsvbl
                }

                .doOnCompleted {
                    shoppingListItemsToCSV("list", items)
                    it.onCompleted()
                }
                .subscribe({ items.addAll(it) }, { ex -> it.onError(ex) })
    }

    override fun importLists(uri: Uri): Completable = Completable.create {

        val items: List<ExporterShoppingListItem>
        try {
            items = shoppingListItemsFromCSV(uri)
        } catch (e: Exception) {
            it.onError(e)
            return@create
        }
        if (items.isEmpty()) {
            it.onCompleted()
            return@create
        }

        val listsMap = mutableMapOf<String, MutableList<ExporterShoppingListItem>>()
        items.forEach {
            if (!listsMap.containsKey(it.getListName())) {
                listsMap[it.getListName()] = mutableListOf()
            }
            listsMap[it.getListName()]!!.add(it)
        }

        var slObs: Observable<ShoppingListItem>? = null
        listsMap.forEach { me ->
            val currSLObs = shopping.createShoppingList(me.key)
                    .onErrorResumeNext {
                        Single.error(handleAuthErrors(logger, auth, resMan, it,
                                "create shopping list"))
                    }
                    .toObservable()

                    .flatMap { list ->
                        var sliObs: Observable<ShoppingListItem>? = null
                        me.value.forEachIndexed { _, item ->
                            val currSLIObs = shopping
                                    .insertShoppingListItem(item.toShoppingListItemInsert(list.id))
                                    .onErrorResumeNext {
                                        Single.error(handleAuthErrors(logger, auth, resMan, it,
                                                "insert shopping list item"))
                                    }
                                    .toObservable()
                            if (sliObs == null) {
                                sliObs = currSLIObs
                                return@forEachIndexed
                            }
                            sliObs = Observable.concat(sliObs!!, currSLIObs)
                        }
                        return@flatMap sliObs
                    }

            if (slObs == null) {
                slObs = currSLObs
                return@forEach
            }
            slObs = Observable.concat(slObs, currSLObs)
        }

        slObs!!
                .doOnCompleted { it.onCompleted() }
                .subscribe({ /*no-op*/ }, { ex -> it.onError(ex) })
    }

    private fun shoppingListItemsFromCSV(uri: Uri): List<ExporterShoppingListItem> {
        val isr = InputStreamReader(files.getInputStream(uri))
        val csvR = CsvBeanReader(isr, CsvPreference.STANDARD_PREFERENCE)

        val items = mutableListOf<ExporterShoppingListItem>()
        csvR.use {
            val headers = it.getHeader(true)
            fileHeadersValid(resMan, headers)
            val processors: Array<CellProcessor?> = arrayOfNulls(headers.size)
            while (true) {
                try {
                    val item = it.read(ExporterShoppingListItem::class.java, headers, *processors)
                            ?: break
                    items.add(item)
                } catch (e: Exception) {
                    logger.warn("Unable to read items from import CSV: " + e.message)
                    throw Exception(resMan.getString(R.string.error_reading_csv))
                }
            }
        }

        return items
    }

    private fun shoppingListItemsToCSV(name: String, items: List<ExporterShoppingListItem>) {

        val file: File
        try {
            file = files.newExternalPublicFile(name, "csv")
        } catch (e: ExternalStorageUnavailableException) {
            throw Exception(resMan.getString(R.string.external_storage_unavailable))
        }

        val osw = OutputStreamWriter(FileOutputStream(file))
        val csvW = CsvBeanWriter(osw, CsvPreference.STANDARD_PREFERENCE)
        try {
            csvW.use {
                it.writeHeader(*conveyanceHeaders)
                items.forEach { item -> it.write(item, *conveyanceHeaders) }
            }
        } catch (e: Exception) {
            throw Exception(resMan.getString(R.string.error_writing_list_items_to_csv))
        }
    }

    companion object {
        val conveyanceHeaders = arrayOf("listName", "itemName", "categoryName", "brandName", "quantity",
                "measuringUnit", "unitPrice", "inList", "inCart")

        fun fileHeadersValid(resMan: ResourceManager, headers: Array<String>) {
            if (!headers.contains("itemName")) {
                throw Exception(resMan.getString(R.string.csv_missing_itemName))
            }
        }
    }

}

data class ExporterShoppingListItem(
        private var listName: String = "General",
        var itemName: String = "",
        var categoryName: String = "",
        var brandName: String = "",
        var quantity: Int = 0,
        var measuringUnit: String = "",
        var unitPrice: Float = 0F,
        var inList: Boolean = false,
        var inCart: Boolean = false
) {
    constructor(listName: String, it: ShoppingListItem) :
            this(listName, it.itemName(), it.categoryName(), it.brandName(), it.quantity,
                    it.measuringUnitName(), it.unitPrice(), it.inList, it.inCart)


    fun toShoppingListItemInsert(listID: String) =
            ShoppingListItemInsert(listID, itemName, inList, inCart, categoryName, brandName,
                    quantity, measuringUnit, unitPrice)

    @Suppress("unused")
    fun setListName(cn: String?) {
        listName = if (cn == null || cn.isBlank()) "General" else cn
    }

    fun getListName() = listName

    @Suppress("unused")
    fun setQuantity(q: String?) {
        quantity = q?.toIntOrNull() ?: 0
    }

    @Suppress("unused")
    fun setUnitPrice(up: String?) {
        unitPrice = up?.toFloatOrNull() ?: 0F
    }

    @Suppress("unused")
    fun setInList(il: String?) {
        inList = il?.toBoolean() ?: false
    }

    @Suppress("unused")
    fun setInCart(ic: String?) {
        inCart = ic?.toBoolean() ?: false
    }
}