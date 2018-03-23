package ke.co.definition.inkopies.repos.ms.shopping

import ke.co.definition.inkopies.model.shopping.ShoppingList
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

    private val shoppingLists = mutableListOf<ShoppingList>()

    override fun getShoppingLists(token: String, offset: Long, count: Int): Single<List<ShoppingList>> {
        return Single.create {
            Thread.sleep(2000)
            if (offset >= shoppingLists.size) {
                it.onError(notFound())
                return@create
            }
            if (Random(Date().time).nextInt(4) == 0) {
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
            if (Random(Date().time).nextInt(4) == 0) {
                it.onError(notFound())
                return@create
            }
            it.onSuccess(sl)
        }
    }

    fun notFound() = HttpException(Response.error<ResponseBody>(STATUS_NOT_FOUND,
            ResponseBody.create(MediaType.parse("text/plain"), "none found")))

}