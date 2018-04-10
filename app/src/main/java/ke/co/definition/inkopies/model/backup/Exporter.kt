package ke.co.definition.inkopies.model.backup

import android.annotation.SuppressLint
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ExternalStorageUnavailableException
import ke.co.definition.inkopies.model.FileHelper
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.utils.logging.Logger
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import rx.Completable
import rx.Observable
import rx.Single
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 09/04/18.
 */
interface Exporter {
    fun exportShoppingLists(): Completable
}

class ExporterImpl @Inject constructor(
        private val shopping: ShoppingManager,
        private val files: FileHelper,
        private val logger: Logger,
        private val auth: Authable,
        private val resMan: ResourceManager
) : Exporter {

    override fun exportShoppingLists(): Completable {

        return shopping.getShoppingLists(0, 100)
                .first()
                .toSingle()
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, auth, resMan, it,
                            "get first shopping lists"))
                }
                .flatMap { shoppingListsToCSV(it) }

                .toObservable()
                .flatMap { lists ->

                    var rsltObsvbl: Observable<Pair<ShoppingList, List<ShoppingListItem>>>? = null
                    lists.forEachIndexed { i, list ->

                        val filter = ShoppingListItemsFilter(list.id)
                        val currObsvbl = shopping.getShoppingListItems(filter, 0, 1000)
                                .toObservable()
                                .onErrorResumeNext {
                                    Observable.error(handleAuthErrors(logger, auth, resMan, it,
                                            "get shopping list items for ${list.name} -> ${list.id}"))
                                }
                                .map { Pair(list, it) }

                        if (i == 0) {
                            rsltObsvbl = currObsvbl
                            return@forEachIndexed
                        }

                        rsltObsvbl = Observable.concat(rsltObsvbl, currObsvbl)
                    }

                    return@flatMap rsltObsvbl
                }

                .map {
                    try {
                        shoppingListItemsToCSV(it.first.name, it.second)
                    } catch (e: Exception) {
                        logger.error("Unable to export shopping list items", e)
                        if (e is ExternalStorageUnavailableException) {
                            throw Exception(resMan.getString(R.string.external_storage_unavailable))
                        }
                        throw Exception(resMan.getString(R.string.error_writing_list_items_to_csv))
                    }
                }

                .toCompletable()
    }

    @SuppressLint("SimpleDateFormat")
    private fun shoppingListsToCSV(lists: List<ShoppingList>) =
            Single.create<List<ShoppingList>> {
                val file = files.newExternalPublicFile("ShoppingLists", "csv")
                val osw = OutputStreamWriter(FileOutputStream(file))
                val csvW = CsvBeanWriter(osw, CsvPreference.STANDARD_PREFERENCE)

                val headers = arrayOf("id", "name", "activeListPrice", "cartPrice", "mode")
                csvW.use {
                    it.writeHeader(*headers)
                    lists.forEach { list ->
                        it.write(list, *headers)
                    }
                }

                it.onSuccess(lists)
            }.onErrorResumeNext {
                logger.error("Unable to export shopping lists", it)
                if (it is ExternalStorageUnavailableException) {
                    return@onErrorResumeNext Single.error(Exception(
                            resMan.getString(R.string.external_storage_unavailable)))
                }
                return@onErrorResumeNext Single.error(
                        Exception(resMan.getString(R.string.error_writing_lists_to_csv)))
            }

    private fun shoppingListItemsToCSV(listName: String, items: List<ShoppingListItem>) {

        val file = files.newExternalPublicFile(listName, "csv")
        val osw = OutputStreamWriter(FileOutputStream(file))
        val csvW = CsvBeanWriter(osw, CsvPreference.STANDARD_PREFERENCE)

        val headers = arrayOf("itemName", "inList", "inCart", "brandName", "quantity",
                "measuringUnit", "unitPrice")
        csvW.use {
            it.writeHeader(*headers)
            items
                    .map {
                        ShoppingListItemInsert("", it.itemName(), it.inList, it.inCart,
                                it.brandName(), it.quantity, it.measuringUnitName(), it.unitPrice())
                    }
                    .forEach { item ->
                        it.write(item, *headers)
                    }
        }
    }

}