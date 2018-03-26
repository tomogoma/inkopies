package ke.co.definition.inkopies.repos.ms.shopping

import android.os.Build
import android.support.annotation.RequiresApi
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.model.stores.Store
import ke.co.definition.inkopies.model.stores.StoreBranch
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava.HttpException
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun updateShoppingListItem(token: String, item: ShoppingListItem): Single<ShoppingListItem> {
        return Single.create {
            Thread.sleep(5000)
            shoppingListItems.replaceAll { if (it.id == item.id) item else it }
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
            list.add(ShoppingListItem(
                    i.toString(),
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
            ))
        }
        return list
    }

    private fun randID(rand: Random) = rand.nextInt().toString()

    private fun randWord(rand: Random): String {
        var letters = ""
        for (i in 0..(rand.nextInt(3) + 2)) {
            letters += Character.toString(randChar(rand))
        }
        return letters
    }

    private fun randChar(rand: Random) = (rand.nextInt(26) + 65).toChar()

}