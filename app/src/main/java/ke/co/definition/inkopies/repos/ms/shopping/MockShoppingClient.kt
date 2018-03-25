package ke.co.definition.inkopies.repos.ms.shopping

import ke.co.definition.inkopies.model.shopping.ShoppingList
import ke.co.definition.inkopies.model.shopping.ShoppingMode
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava.HttpException
import rx.Single

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

    override fun getShoppingLists(token: String, offset: Long, count: Int): Single<List<ShoppingList>> {
        return Single.create {
            Thread.sleep(2000)
            if (offset >= shoppingLists.size) {
                it.onError(notFound())
                return@create
            }
            var lastIndx = offset.toInt() + count
            if (lastIndx > shoppingLists.size) {
                lastIndx = shoppingLists.size
            }
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

}