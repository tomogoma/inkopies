package ke.co.definition.inkopies.repos.firestore

import com.google.firebase.firestore.*
import ke.co.definition.inkopies.model.auth.JWTHelper
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.repos.ms.STATUS_CONFLICT
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Single
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 03/04/18.
 */
class FirebaseShoppingClient @Inject constructor(
        private val jwtHelper: JWTHelper,
        private val db: FirebaseFirestore
) : ShoppingClient {

    private val collItems = db.collection(COLLECTION_ITEMS)
    private val collMeasUnits = db.collection(COLLECTION_MEASURING_UNITS)
    private val collBrands = db.collection(COLLECTION_BRANDS)
    private val collPrices = db.collection(COLLECTION_PRICES)

    override fun addShoppingList(token: String, name: String) = Single.create<ShoppingList> {
        val doc = FirestoreShoppingList(name)
        collShoppingLists(token)
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toShoppingList(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    override fun updateShoppingList(token: String, list: ShoppingList) = Single.create<ShoppingList> {
        val doc = FirestoreShoppingList(list)
        collShoppingLists(token)
                .document(list.id)
                .set(doc)
                .addOnSuccessListener { _ ->
                    it.onSuccess(doc.toShoppingList(list.id))
                }
                .addOnFailureListener(it::onError)
    }

    override fun getShoppingLists(token: String, offset: Long, count: Int) = Single.create<List<ShoppingList>> {
        collShoppingLists(token)
                .limit(count.toLong())
                .get()
                .addOnSuccessListener { qs: QuerySnapshot ->
                    if (qs.isEmpty) {
                        it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                        return@addOnSuccessListener
                    }
                    val res: MutableList<ShoppingList> = mutableListOf()
                    qs.forEach {
                        res.add(
                                it.toObject(FirestoreShoppingList::class.java)
                                        .toShoppingList(it.id)
                        )
                    }
                    it.onSuccess(res)
                }
                .addOnFailureListener { e: Exception ->
                    it.onError(httpException(STATUS_NOT_FOUND, e.message ?: "reported by firebase"))
                }
    }

    override fun insertShoppingListItem(token: String, req: ShoppingListItemInsert): Single<ShoppingListItem> {
        var measUnit: MeasuringUnit? = null
        var price: BrandPrice? = null

        return insertMeasUnitIfNotExists(req.measuringUnit ?: "")
                .map { measUnit = it.second.toMeasuringUnit(it.first) }

                .flatMap { insertItemIfNotExists(req.itemName) }
                .map { it.second.toShoppingItem(it.first) }

                .flatMap {
                    insertBrandIfNotExists(measUnit!!, it, req.brandName ?: "")
                }

                .flatMap { insertPriceIfNotExists(req.unitPrice ?: 0F, it) }
                .map { price = it }

                .flatMap {
                    insertShoppingListItemIfNotExists(token, req.shoppingListID,
                            req.quantity ?: 0, req.inList, req.inCart, price!!)
                }
    }

    override fun updateShoppingListItem(token: String, update: ShoppingListItemUpdate): Single<ShoppingListItem> {

        var curr: ShoppingListItem? = null
        var observer = getShoppingListItem(token, update.shoppingListID, update.shoppingListItemID)
                .map { curr = it }

        var item: ShoppingItem? = null
        if (update.itemName != null) {
            observer = observer.flatMap { insertItemIfNotExists(update.itemName) }
                    .map { item = it.second.toShoppingItem(it.first) }
        }

        var measUnit: MeasuringUnit? = null
        if (update.measuringUnit != null) {
            observer = observer.flatMap { insertMeasUnitIfNotExists(update.measuringUnit) }
                    .map { measUnit = it.second.toMeasuringUnit(it.first) }
        }

        // An update for brand has to be created whether provided or not
        // as it also holds possible updates of measuringUnit and shoppingItem.
        var brand: Brand? = null
        if (update.brandName != null) {
            observer = observer
                    .flatMap {
                        insertBrandIfNotExists(measUnit ?: curr!!.measuringUnit(),
                                item ?: curr!!.item(), update.brandName)
                    }
                    .map { brand = it }
        } else {
            observer = observer.map {
                brand = Brand(curr!!.brand().id, curr!!.brandName(),
                        measUnit ?: curr!!.measuringUnit(),
                        item ?: curr!!.item())
            }
        }

        // An update for brandPrice has to be created whether provided or not
        // as it holds possible updates of brand
        // (which may have possible updates of other fields).
        var price: BrandPrice? = null
        if (update.unitPrice != null) {
            observer = observer
                    .flatMap {
                        insertPriceIfNotExists(update.unitPrice, brand!!)
                    }
                    .map { price = it }
        } else {
            observer = observer.map {
                price = BrandPrice(curr!!.id, curr!!.unitPrice(), brand!!)
            }
        }

        // finally overwrite the existing shoppingListItem.
        return observer
                .flatMap { _ ->
                    Single.create<ShoppingListItem> {
                        val doc = FirestoreShoppingListItem(
                                update.quantity ?: curr!!.quantity,
                                update.inList ?: curr!!.inList,
                                update.inCart ?: curr!!.inCart,
                                price!!
                        )
                        collShoppingListItems(token, update.shoppingListID)
                                .document(update.shoppingListItemID)
                                .set(doc)
                                .addOnSuccessListener { _ ->
                                    it.onSuccess(doc.toShoppingListItem(update.shoppingListItemID))
                                }
                                .addOnFailureListener(it::onError)
                    }
                }
    }

    override fun deleteShoppingListItem(token: String, shoppingListID: String, id: String) = Completable.create {
        collShoppingListItems(token, shoppingListID)
                .document(id)
                .delete()
                .addOnSuccessListener { _ -> it.onCompleted() }
                .addOnFailureListener(it::onError)
    }

    override fun getShoppingListItems(token: String, f: ShoppingListItemsFilter, offset: Long, count: Int) =
            Single.create<List<ShoppingListItem>> {
                var query = collShoppingListItems(token, f.shoppingListID)
                        .limit(count.toLong())
                if (f.inList != null) {
                    query = query.whereEqualTo(FirestoreShoppingListItem.KEY_IN_LIST, f.inList)
                }
                query.get()
                        .addOnSuccessListener { qs: QuerySnapshot ->
                            if (qs.isEmpty) {
                                it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                                return@addOnSuccessListener
                            }
                            val res = qs.documents.map {
                                it.toObject(FirestoreShoppingListItem::class.java)
                                        .toShoppingListItem(it.id)
                            }
                            it.onSuccess(res)
                        }
                        .addOnFailureListener { e: Exception ->
                            it.onError(httpException(STATUS_NOT_FOUND, e.message
                                    ?: "reported by firebase"))
                        }
            }

    override fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>> {
        return Single.error(httpException(STATUS_NOT_FOUND, "not even implemented"))
    }

    private fun insertShoppingListItemIfNotExists(token: String, shoppingListID: String,
                                                  quantity: Int, inList: Boolean, inCart: Boolean,
                                                  price: BrandPrice): Single<ShoppingListItem> {

        var hasInserted = false
        return getShoppingListItemByPriceID(token, shoppingListID, price.id)
                .onErrorResumeNext { e: Throwable ->
                    if (!isNotFound(e)) {
                        return@onErrorResumeNext Single.error(e)
                    }
                    hasInserted = true
                    return@onErrorResumeNext insertShoppingListItem(token, shoppingListID,
                            quantity, inList, inCart, price)
                }
                .flatMap {
                    if (hasInserted) {
                        return@flatMap Single.just(it)
                    }
                    return@flatMap Single.error<ShoppingListItem>(httpException(STATUS_CONFLICT,
                            "shopping list item exists"))
                }
    }

    private fun insertShoppingListItem(token: String, shoppingListID: String, quantity: Int,
                                       inList: Boolean, inCart: Boolean,
                                       price: BrandPrice) = Single.create<ShoppingListItem> {
        val doc = FirestoreShoppingListItem(quantity, inList, inCart, price)
        collShoppingListItems(token, shoppingListID)
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toShoppingListItem(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getShoppingListItem(token: String, shoppingListID: String, ID: String) = Single.create<ShoppingListItem> {
        collShoppingListItems(token, shoppingListID)
                .document(ID)
                .get()
                .addOnSuccessListener { ds: DocumentSnapshot ->
                    val fsListItem = ds.toObject(FirestoreShoppingListItem::class.java)
                    it.onSuccess(fsListItem.toShoppingListItem(ds.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getShoppingListItemByPriceID(token: String, shoppingListID: String, priceID: String) = Single.create<ShoppingListItem> {
        collShoppingListItems(token, shoppingListID)
                .whereEqualTo(FirestoreShoppingListItem.KEY_BRAND_PRICE_ID, priceID)
                .get()
                .addOnSuccessListener { qs: QuerySnapshot ->
                    if (qs.isEmpty) {
                        it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                        return@addOnSuccessListener
                    }
                    val ss = qs.elementAt(0)
                    val fsListItem = ss.toObject(FirestoreShoppingListItem::class.java)
                    it.onSuccess(fsListItem.toShoppingListItem(ss.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun insertPriceIfNotExists(price: Float, brand: Brand) =
            getPrice(price, brand.id)
                    .onErrorResumeNext { e: Throwable ->
                        if (!isNotFound(e)) {
                            return@onErrorResumeNext Single.error(e)
                        }
                        return@onErrorResumeNext insertPrice(price, brand)
                    }

    private fun insertPrice(price: Float, brand: Brand) = Single.create<BrandPrice> {
        val doc = FirestorePrice(price, brand)
        collPrices
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toPrice(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getPrice(price: Float, brandID: String) = Single.create<BrandPrice> {
        collPrices
                .whereEqualTo(FirestorePrice.KEY_PRICE, price)
                .whereEqualTo(FirestorePrice.KEY_BRAND_ID, brandID)
                .get()
                .addOnSuccessListener { qs: QuerySnapshot ->
                    if (qs.isEmpty) {
                        it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                        return@addOnSuccessListener
                    }
                    val ss = qs.elementAt(0)
                    val fsPrice = ss.toObject(FirestorePrice::class.java)
                    it.onSuccess(fsPrice.toPrice(ss.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun insertBrandIfNotExists(measUnit: MeasuringUnit, item: ShoppingItem, name: String) =
            getBrand(name, measUnit.id, item.id)
                    .onErrorResumeNext { e: Throwable ->
                        if (!isNotFound(e)) {
                            return@onErrorResumeNext Single.error(e)
                        }
                        return@onErrorResumeNext insertBrand(name, measUnit, item)
                    }

    private fun insertBrand(name: String, measUnit: MeasuringUnit, item: ShoppingItem) = Single.create<Brand> {
        val doc = FirestoreBrand(name, measUnit, item)
        collBrands
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toBrand(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getBrand(name: String, measUnitID: String, itemID: String) = Single.create<Brand> {
        collBrands
                .whereEqualTo(FirestoreBrand.KEY_NAME, name)
                .whereEqualTo(FirestoreBrand.KEY_MEASURING_UNIT_ID, measUnitID)
                .whereEqualTo(FirestoreBrand.KEY_ITEM_ID, itemID)
                .get()
                .addOnSuccessListener { qs: QuerySnapshot ->
                    if (qs.isEmpty) {
                        it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                        return@addOnSuccessListener
                    }
                    val ss = qs.elementAt(0)
                    val fsBrand = ss.toObject(FirestoreBrand::class.java)
                    it.onSuccess(fsBrand.toBrand(ss.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun insertItemIfNotExists(name: String) =
            insertNamedIfNotExists(collItems, name)

    private fun insertMeasUnitIfNotExists(name: String) =
            insertNamedIfNotExists(collMeasUnits, name)

    private fun insertNamedIfNotExists(collection: CollectionReference, name: String) =
            getNamed(collection, name)
                    .onErrorResumeNext { e: Throwable ->
                        if (!isNotFound(e)) {
                            return@onErrorResumeNext Single.error(e)
                        }
                        return@onErrorResumeNext insertNamed(collection, name)
                    }

    private fun insertNamed(collection: CollectionReference, name: String) = Single.create<Pair<String, Named>> {
        val doc = Named(name)
        collection.add(Named(name))
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(Pair(dr.id, doc))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getNamed(collection: CollectionReference, name: String) = Single.create<Pair<String, Named>> {
        collection.whereEqualTo(Named.KEY_NAME, name)
                .get()
                .addOnSuccessListener { qs: QuerySnapshot ->
                    if (qs.isEmpty) {
                        it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                        return@addOnSuccessListener
                    }
                    val ss = qs.elementAt(0)
                    it.onSuccess(Pair(ss.id, ss.toObject(Named::class.java)))
                }
                .addOnFailureListener(it::onError)
    }

    private fun isNotFound(e: Throwable) = e is HttpException && e.code() == STATUS_NOT_FOUND

    private fun httpException(code: Int, msg: String) = HttpException(
            Response.error<ResponseBody>(code,
                    ResponseBody.create(MediaType.parse("text/plain"), msg))
    )

    private fun collShoppingLists(token: String): CollectionReference {
        val jwt = jwtHelper.extractJWT(token)
        return db.collection(COLLECTION_USERS)
                .document(jwt.info.userID)
                .collection(COLLECTION_SHOPPING_LISTS)
    }

    private fun collShoppingListItems(token: String, shoppingListID: String): CollectionReference {
        return collShoppingLists(token)
                .document(shoppingListID)
                .collection(COLLECTION_SHOPPING_LIST_ITEMS)
    }

    companion object {

        const val COLLECTION_USERS = "users"
        const val COLLECTION_SHOPPING_LISTS = "shopping_lists"
        const val COLLECTION_SHOPPING_LIST_ITEMS = "shopping_list_items"
        const val COLLECTION_PRICES = "prices"
        const val COLLECTION_BRANDS = "brands"
        const val COLLECTION_MEASURING_UNITS = "measuring_units"
        const val COLLECTION_ITEMS = "items"
    }

}

data class FirestoreShoppingList(
        val name: String = "",
        val activeListPrice: Float = 0F,
        val cartPrice: Float = 0F,
        val mode: String = ShoppingMode.PREPARATION.name
) {

    constructor(list: ShoppingList) :
            this(list.name, list.activeListPrice, list.cartPrice, list.mode.name)

    fun toShoppingList(id: String) = ShoppingList(id, name, activeListPrice, cartPrice,
            ShoppingMode.valueOf(mode))
}

data class FirestoreShoppingListItem(
        val quantity: Int = 0,
        val inList: Boolean = false,
        val inCart: Boolean = false,
        val priceID: String = "",
        val price: FirestorePrice = FirestorePrice()
) {

    constructor(quantity: Int, inList: Boolean, inCart: Boolean, price: BrandPrice) :
            this(quantity, inList, inCart, price.id, FirestorePrice(price))

    fun toShoppingListItem(id: String) = ShoppingListItem(id, quantity, price.toPrice(priceID), inList, inCart)

    companion object {
        const val KEY_BRAND_PRICE_ID = "brandPriceId"
        const val KEY_IN_LIST = "inList"
    }
}

data class FirestorePrice(
        val price: Float = 0F,
        val brandID: String = "",
        val brand: FirestoreBrand = FirestoreBrand()
) {

    constructor(price: BrandPrice) : this(price.price, price.brand)

    constructor(price: Float, brand: Brand) :
            this(price, brand.id, FirestoreBrand(brand))

    fun toPrice(id: String) = BrandPrice(id, price, brand.toBrand(brandID))

    companion object {
        const val KEY_PRICE = "price"
        const val KEY_BRAND_ID = "brandId"
    }
}

data class FirestoreBrand(
        val name: String = "",
        val measuringUnitID: String = "",
        val measuringUnit: Named = Named(),
        val shoppigItemID: String = "",
        val shoppingItem: Named = Named()
) {

    constructor(brand: Brand) : this(brand.name, brand.measuringUnit, brand.shoppingItem)

    constructor(name: String, measUnit: MeasuringUnit, item: ShoppingItem) :
            this(name, measUnit.id, Named(measUnit.name), item.id, Named(item.name))

    fun toBrand(id: String) = Brand(id, name, measuringUnit.toMeasuringUnit(measuringUnitID),
            shoppingItem.toShoppingItem(shoppigItemID))

    companion object {
        const val KEY_NAME = "name"
        const val KEY_ITEM_ID = "shoppigItemId"
        const val KEY_MEASURING_UNIT_ID = "measuringUnitId"
    }
}

data class Named(val name: String = "") {

    fun toShoppingItem(id: String) = ShoppingItem(id, name)
    fun toMeasuringUnit(id: String) = MeasuringUnit(id, name)

    companion object {
        const val KEY_NAME = "name"
    }
}