package ke.co.definition.inkopies.repos.ms.shopping

import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.model.stores.Store
import ke.co.definition.inkopies.model.stores.StoreBranch
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Single
import java.util.*

/**
 * Created by tomogoma
 * On 23/03/18.
 */
class MockShoppingClient : ShoppingClient {

    private val shoppingLists = mutableListOf(
            ShoppingList("1", "One", 500F, 100F, ShoppingMode.SHOPPING),
            ShoppingList("2", "Two", 0F, 0F, ShoppingMode.PREPARATION),
            ShoppingList("3", "Three", 2500F, 0F, ShoppingMode.SHOPPING)
    )

    private var shoppingListItems = mutableListOf<ShoppingListItem>()

    override fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>> {
        return Single.create {
            val rslts = shoppingListItems.filter {
                (req.brandName != null && req.brandName != "" && it.brandName().toLowerCase().contains(req.brandName.toLowerCase()))
                        || (req.shoppingItemName != null && req.shoppingItemName != "" && it.itemName().toLowerCase().contains(req.shoppingItemName.toLowerCase()))
                        || (req.brandPrice != null && req.brandPrice != "" && it.unitPrice().toString().toLowerCase().contains(req.brandPrice.toLowerCase()))
                        || (req.measUnit != null && req.measUnit != "" && it.measuringUnitName().toLowerCase().contains(req.measUnit.toLowerCase()))
            }
            if (rslts.isEmpty()) {
                it.onError(notFound())
                return@create
            }

            it.onSuccess(rslts)
        }
    }

    override fun upsertShoppingListItem(token: String, req: ShoppingListItemUpsert): Single<ShoppingListItem> {
        return Single.create {
            Thread.sleep(2000)
            var rslt: ShoppingListItem?
            try {
                val indx = getShoppingListItemIndex(req.itemID ?: throw notFound())
                val curr = shoppingListItems[indx]
                rslt = ShoppingListItem(
                        curr.id, req.quantity ?: curr.quantity,
                        ShoppingList(req.shoppingListID, curr.shoppingList.name,
                                curr.shoppingList.activeListPrice, curr.shoppingList.cartPrice,
                                curr.shoppingList.mode),
                        BrandPrice(curr.brandPrice.id, req.unitPrice ?: curr.unitPrice(),
                                Brand(curr.brandPrice.brand.id, req.brandName ?: curr.brandName(),
                                        MeasuringUnit(curr.brandPrice.brand.measuringUnit.id,
                                                req.measuringUnit ?: curr.measuringUnitName()),
                                        ShoppingItem(curr.brandPrice.brand.shoppingItem.id,
                                                req.itemName)),
                                curr.brandPrice.atStoreBranch),
                        req.inList, req.inCart
                )
                shoppingListItems[indx] = rslt
            } catch (e: HttpException) {
                val rand = Random(Date().time)
                rslt = ShoppingListItem(
                        randID(rand), req.quantity ?: 0,
                        getShoppingList(req.shoppingListID),
                        BrandPrice(randID(rand), req.unitPrice ?: 0F,
                                Brand(randID(rand), req.brandName ?: "",
                                        MeasuringUnit(randID(rand), req.measuringUnit ?: ""),
                                        ShoppingItem(randID(rand), req.itemName)),
                                StoreBranch(randID(rand), randWord(rand),
                                        Store(randID(rand), randWord(rand)))),
                        req.inList, req.inCart
                )
                shoppingListItems.add(rslt)
            }

            it.onSuccess(rslt)
        }
    }

    private fun getShoppingList(id: String): ShoppingList {
        return shoppingLists.first { it.id == id }
    }

    override fun deleteShoppingListItem(token: String, id: String): Completable {
        return Completable.create {
            Thread.sleep(2000)
            val indx = getShoppingListItemIndex(id)
            shoppingListItems.removeAt(indx)
            it.onCompleted()
        }
    }

    override fun updateShoppingListItem(token: String, item: ShoppingListItem): Single<ShoppingListItem> {
        return Single.create {
            Thread.sleep(2000)
            val indx = getShoppingListItemIndex(item.id)
            shoppingListItems[indx] = item
            it.onSuccess(item)
        }
    }

    override fun getShoppingListItems(token: String, shoppingListID: String, offset: Long, count: Int): Single<List<ShoppingListItem>> {
        return Single.create {
            Thread.sleep(2000)
            if (shoppingListItems.size == 0) {
                shoppingListItems = genShoppingListItems(shoppingListID)
            }
            if (offset >= shoppingListItems.size) {
                it.onError(notFound())
                return@create
            }
            val lastIndx = calcLastIndex(offset, count, shoppingListItems.size)
            it.onSuccess(shoppingListItems.subList(offset.toInt(), lastIndx))
        }
    }

    override fun getShoppingLists(token: String, offset: Long, count: Int): Single<List<ShoppingList>> {
        return Single.create {
            Thread.sleep(2000)
            if (offset >= shoppingLists.size) {
                it.onError(notFound())
                return@create
            }
            val lastIndx = calcLastIndex(offset, count, shoppingLists.size)
            it.onSuccess(shoppingLists.subList(offset.toInt(), lastIndx))
        }
    }

    override fun addShoppingList(token: String, name: String): Single<ShoppingList> {
        return Single.create {
            val sl = ShoppingList(shoppingLists.size.toString(), name, 0f, 0f)
            shoppingLists.add(sl)
            Thread.sleep(2000)
            it.onSuccess(sl)
        }
    }

    private fun getShoppingListItemIndex(id: String): Int {
        var indx = -1
        shoppingListItems.forEachIndexed { i, itm -> if (itm.id == id) indx = i }
        if (indx == -1) {
            throw notFound()
        }
        return indx
    }

    private fun notFound() = HttpException(Response.error<ResponseBody>(STATUS_NOT_FOUND,
            ResponseBody.create(MediaType.parse("text/plain"), "none found")))

    private fun calcLastIndex(offset: Long, count: Int, listSize: Int): Int {
        var lastIndx = offset.toInt() + count
        if (lastIndx > listSize) {
            lastIndx = listSize
        }
        return lastIndx
    }

    private fun genShoppingListItems(shoppingListID: String): MutableList<ShoppingListItem> {
        val list = mutableListOf<ShoppingListItem>()
        val rand = Random(Date().time)
        for (i in 0..1000) {
            list.add(genShoppingListItem(shoppingListID, i.toString(), rand))
        }
        return list
    }

    fun genShoppingListItem(shoppingListID: String, id: String, rand: Random) = ShoppingListItem(
            id,
            rand.nextInt(5),
            ShoppingList(shoppingListID, randWord(rand), 0F, 0F,
                    ShoppingMode.PREPARATION),
            BrandPrice(
                    randID(rand),
                    rand.nextFloat(),
                    Brand(randID(rand), randWord(rand),
                            MeasuringUnit(randID(rand), randWord(rand)),
                            ShoppingItem(randID(rand), randWord(rand))),
                    StoreBranch(randID(rand), randWord(rand), Store(randID(rand),
                            randWord(rand)))
            ),
            rand.nextBoolean(),
            rand.nextBoolean()
    )

    private fun randID(rand: Random) = rand.nextInt().toString()

    private fun randWord(rand: Random): String {
        var letters = ""
        for (i in 0..(rand.nextInt(7) + 2)) {
            letters += Character.toString(
                    if (rand.nextInt(3) == 0) {
                        randVowel(rand)
                    } else {
                        randConsonant(rand)
                    }
            )
        }
        return letters.capitalize()
    }

    private fun randVowel(rand: Random) = vowels[rand.nextInt(vowels.length)]
    private fun randConsonant(rand: Random) = consonants[rand.nextInt(consonants.length)]

}

const val vowels = "aeiou"
const val consonants = "bcdfghjklmnpqrstvwxyz"