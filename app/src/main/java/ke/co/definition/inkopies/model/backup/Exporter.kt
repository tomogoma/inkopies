package ke.co.definition.inkopies.model.backup

import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ExternalStorageUnavailableException
import ke.co.definition.inkopies.model.FileHelper
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingListItem
import ke.co.definition.inkopies.model.shopping.ShoppingListItemsFilter
import ke.co.definition.inkopies.model.shopping.ShoppingManager
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.utils.logging.Logger
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import rx.Completable
import rx.Observable
import rx.Single
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 09/04/18.
 */
interface Exporter {
    fun exportShoppingLists(): Completable
    fun exportShoppingList(list: ShoppingList): Completable
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

    private fun shoppingListItemsToCSV(name: String, items: List<ExporterShoppingListItem>) {

        val file: File
        try {
            file = files.newExternalPublicFile(name, "csv")
        } catch (e: ExternalStorageUnavailableException) {
            throw Exception(resMan.getString(R.string.external_storage_unavailable))
        }

        val osw = OutputStreamWriter(FileOutputStream(file))
        val csvW = CsvBeanWriter(osw, CsvPreference.STANDARD_PREFERENCE)

        val headers = arrayOf("listName", "itemName", "categoryName", "brandName", "quantity",
                "measuringUnit", "unitPrice", "inList", "inCart")
        try {
            csvW.use {
                it.writeHeader(*headers)
                items.forEach { item -> it.write(item, *headers) }
            }
        } catch (e: Exception) {
            throw Exception(resMan.getString(R.string.error_writing_list_items_to_csv))
        }
    }

}

data class ExporterShoppingListItem(
        val listName: String,
        val itemName: String,
        val categoryName: String,
        val brandName: String,
        val quantity: Int,
        val measuringUnit: String,
        val unitPrice: Float,
        val inList: Boolean,
        val inCart: Boolean
) {
    constructor(listName: String, it: ShoppingListItem) :
            this(listName, it.itemName(), it.categoryName(), it.brandName(), it.quantity,
                    it.measuringUnitName(), it.unitPrice(), it.inList, it.inCart)
}