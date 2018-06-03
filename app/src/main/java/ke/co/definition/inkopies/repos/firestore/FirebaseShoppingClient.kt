package ke.co.definition.inkopies.repos.firestore

import com.google.firebase.firestore.*
import ke.co.definition.inkopies.model.auth.JWTHelper
import ke.co.definition.inkopies.model.shopping.*
import ke.co.definition.inkopies.model.stores.Store
import ke.co.definition.inkopies.model.stores.StoreBranch
import ke.co.definition.inkopies.repos.ms.STATUS_CONFLICT
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import ke.co.definition.inkopies.repos.ms.shopping.ShoppingClient
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Observable
import rx.Single
import java.util.*
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 03/04/18.
 */
class FirebaseShoppingClient @Inject constructor(
        private val jwtHelper: JWTHelper,
        private val db: FirebaseFirestore
) : ShoppingClient {

    private val collStores = db.collection(COLLECTION_STORES)
    private val collStoreBranches = db.collection(COLLECTION_STORE_BRANCHES)
    private val collCategories = db.collection(COLLECTION_CATEGORIES)
    private val collItems = db.collection(COLLECTION_ITEMS)
    private val collMeasUnits = db.collection(COLLECTION_MEASURING_UNITS)
    private val collBrands = db.collection(COLLECTION_BRANDS)
    private val collPrices = db.collection(COLLECTION_PRICES)

    override fun addShoppingList(token: String, name: String): Single<ShoppingList> = Single.create<ShoppingList> {
        val doc = FirestoreShoppingList(name)
        collShoppingLists(token)
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toShoppingList(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    override fun updateShoppingList(token: String, list: ShoppingList): Single<ShoppingList> = Single.create<ShoppingList> {
        val doc = FirestoreShoppingList(list)
        collShoppingLists(token)
                .document(list.id)
                .set(doc)
                .addOnSuccessListener { _ ->
                    it.onSuccess(doc.toShoppingList(list.id))
                }
                .addOnFailureListener(it::onError)
    }

    override fun getShoppingLists(token: String, offset: Long, count: Int): Observable<List<ShoppingList>> {
        var listenerReg: ListenerRegistration? = null
        return Observable.create<List<ShoppingList>> {

            listenerReg = collShoppingLists(token)
                    .orderBy(FirestoreShoppingList.KEY_NAME)
//                .limit(count.toLong())
                    .addSnapshotListener { qs, e ->
                        if (e != null) {
                            it.onError(Exception("Unable to fetch items: ${e.message} - ${e.cause}"))
                            return@addSnapshotListener
                        }
                        if (qs!!.isEmpty) {
                            it.onNext(listOf())
                            return@addSnapshotListener
                        }
                        val res = qs.map {
                            it.toObject(FirestoreShoppingList::class.java).toShoppingList(it.id)
                        }
                        it.onNext(res)
                    }
        }
                .doOnTerminate { listenerReg?.remove() }
    }

    override fun getShoppingList(token: String, id: String): Observable<ShoppingList> {
        var listenerReg: ListenerRegistration? = null
        return Observable.create<ShoppingList> {
            listenerReg = collShoppingLists(token).document(id)
                    .addSnapshotListener { ds, e ->
                        if (e != null) {
                            it.onError(Exception("Unable to fetch items: ${e.message} - ${e.cause}"))
                            return@addSnapshotListener
                        }
                        if (!ds!!.exists()) {
                            it.onError(httpException(STATUS_NOT_FOUND, "Shopping list not found"))
                            return@addSnapshotListener
                        }
                        val sl = ds.toObject(FirestoreShoppingList::class.java)!!
                                .toShoppingList(id)
                        it.onNext(sl)
                    }
        }.doOnTerminate { listenerReg?.remove() }
    }

    override fun insertShoppingListItem(token: String, req: ShoppingListItemInsert): Single<ShoppingListItem> {
        var measUnit: MeasuringUnit? = null
        var price: BrandPrice? = null
        var cat: Category? = null
        var branch: StoreBranch? = null

        return insertMeasUnitIfNotExists(req.measuringUnit ?: "")
                .map { measUnit = it.second.toMeasuringUnit(it.first) }

                .flatMap { insertCategoryIfNotExist(req.categoryName ?: "") }
                .map { cat = it.second.toCategory(it.first) }

                .flatMap { insertStoreIfNotExist("") }
                .map { it.second.toStore(it.first) }

                .flatMap { insertBranchIfNotExists("", it) }
                .map { branch = it }

                .flatMap { insertItemIfNotExists(req.itemName) }
                .map { it.second.toShoppingItem(it.first) }

                .flatMap {
                    insertBrandIfNotExists(measUnit!!, it, req.brandName ?: "")
                }

                .flatMap { insertPriceIfNotExists(req.unitPrice ?: 0F, it, branch!!) }
                .map { price = it }

                .flatMap {
                    insertShoppingListItemIfNotExists(token, req.shoppingListID, cat!!,
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

        var cat: Category? = null
        if (update.categoryName != null) {
            observer = observer.flatMap { insertCategoryIfNotExist(update.categoryName) }
                    .map { cat = it.second.toCategory(it.first) }
        }

        var store: Store? = null
        if (update.storeName != null) {
            observer = observer.flatMap { insertStoreIfNotExist(update.storeName) }
                    .map { store = it.second.toStore(it.first) }
        }

        // An update for store branch has to be created whether provided or not
        // as it also holds possible updates of store.
        var storeBranch: StoreBranch? = null
        if (update.storeBranchName != null) {
            observer = observer
                    .flatMap {
                        insertBranchIfNotExists(update.storeBranchName,
                                store ?: curr!!.store())
                    }
                    .map { storeBranch = it }
        } else {
            observer = observer.map {
                storeBranch = StoreBranch(curr!!.storeBranchID(), curr!!.storeBranchName(),
                        store ?: curr!!.store())
            }
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
                brand = Brand(curr!!.brandID(), curr!!.brandName(),
                        measUnit ?: curr!!.measuringUnit(),
                        item ?: curr!!.item())
            }
        }

        // An update for brandPrice has to be created whether provided or not
        // as it holds possible updates of brand and storeBranch
        // (which may have possible updates of other fields).
        var price: BrandPrice? = null
        if (update.unitPrice != null) {
            observer = observer
                    .flatMap {
                        insertPriceIfNotExists(update.unitPrice, brand!!, storeBranch!!)
                    }
                    .map { price = it }
        } else {
            observer = observer.map {
                price = BrandPrice(curr!!.id, curr!!.unitPrice(), brand!!, storeBranch!!)
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
                                price!!, cat!!
                        )
                        db
                                .runTransaction { tx ->

                                    val shoppingListRef = collShoppingLists(token)
                                            .document(update.shoppingListID)
                                    val shoppingList = getShoppingList(tx, shoppingListRef)
                                            .accumulateUpdatePrices(curr!!, update)
                                    tx.set(shoppingListRef, FirestoreShoppingList(shoppingList))

                                    val listItemRef = collShoppingListItems(token, update.shoppingListID)
                                            .document(update.shoppingListItemID)
                                    tx.set(listItemRef, doc)

                                    return@runTransaction null
                                }
                                .addOnSuccessListener { _ ->
                                    it.onSuccess(doc.toShoppingListItem(update.shoppingListItemID))
                                }
                                .addOnFailureListener(it::onError)
                    }
                }
    }

    override fun deleteShoppingListItem(token: String, shoppingListID: String, id: String): Completable = Completable.create {
        db
                .runTransaction { tx ->

                    val listItemRef = collShoppingListItems(token, shoppingListID)
                            .document(id)
                    val listItem = tx.get(listItemRef)
                            .toObject(FirestoreShoppingListItem::class.java)!!
                            .toShoppingListItem(id)

                    val shoppingListRef = collShoppingLists(token).document(shoppingListID)
                    val shoppingList = tx.get(shoppingListRef)
                            .toObject(FirestoreShoppingList::class.java)!!
                            .toShoppingList(shoppingListID)
                            .accumulateDeletePrices(listItem)
                    tx.set(shoppingListRef, FirestoreShoppingList(shoppingList))

                    tx.delete(listItemRef)

                    return@runTransaction null
                }
                .addOnSuccessListener { _ -> it.onCompleted() }
                .addOnFailureListener(it::onError)
    }

    override fun getShoppingListItems(token: String, f: ShoppingListItemsFilter): Observable<List<ShoppingListItem>> {
        var listenerReg: ListenerRegistration? = null
        return Observable.create<List<ShoppingListItem>> {
            val query = if (f.inList != null || f.inCart != null) {
                var q: Query = collShoppingListItems(token, f.shoppingListID)
                if (f.inList != null) {
                    q = q.whereEqualTo(FirestoreShoppingListItem.KEY_IN_LIST, f.inList)
                }
                if (f.inCart != null) {
                    q = q.whereEqualTo(FirestoreShoppingListItem.KEY_IN_CART, f.inCart)
                }
                q
//                      .orderBy(FirestoreShoppingListItem.KEY_IN_CART, Query.Direction.DESCENDING)
            } else {
                collShoppingListItems(token, f.shoppingListID)
//                            .orderBy(FirestoreShoppingListItem.KEY_IN_LIST, Query.Direction.DESCENDING)
            }

            listenerReg = query
//                        .orderBy(FirestoreShoppingListItem.KEY_ITEM_NAME)
//                        .limit(count.toLong())
                    .addSnapshotListener { qs, e ->
                        if (e != null) {
                            it.onError(Exception("Unable to fetch items: ${e.message} - ${e.cause}"))
                            return@addSnapshotListener
                        }
                        if (qs!!.isEmpty) {
                            it.onNext(listOf())
                            return@addSnapshotListener
                        }
                        val res = qs
                                .map {
                                    it.toObject(FirestoreShoppingListItem::class.java)
                                            .toShoppingListItem(it.id)
                                }
                                .sortedBy { it.itemName() }
                                .sortedBy { it.categoryName() }
                                .sortedByDescending {
                                    if (f.inList != null) it.inCart
                                    else it.inList
                                }
                        it.onNext(res)
                    }
        }.doOnTerminate { listenerReg?.remove() }
    }


    override fun checkout(token: String, slid: String, branchName: String?, storeName: String?, date: Date): Completable {
        var checkoutItems: List<ShoppingListItem>? = null
        var branch: StoreBranch? = null
        var shoppingList: ShoppingList? = null
        val shoppingListRef = collShoppingLists(token).document(slid)
        val sliFilter = ShoppingListItemsFilter(shoppingListID = slid, inList = true,
                inCart = true)
        return insertStoreIfNotExist(storeName ?: "")
                .map { it.second.toStore(it.first) }

                .flatMap { insertBranchIfNotExists(branchName ?: "", it) }
                .map { branch = it }

                .flatMap {
                    getShoppingListItems(token, sliFilter)
                            .first()
                            .toSingle()
                }

                .flatMap { cartItems ->

                    // 1.
                    // a. insert prices, attaching them to the store-branch
                    // b. remove shopping list items from cart
                    var obs: Observable<ShoppingListItem>? = null

                    cartItems!!.forEach { cartItm ->
                        val price = BrandPrice(cartItm.id, cartItm.unitPrice(),
                                cartItm.brand(), branch!!)
                        val currObs = insertPriceIfNotExists(price.price, price.brand, branch!!)
                                .map {
                                    ShoppingListItem(cartItm.id, cartItm.quantity, it,
                                            cartItm.category, cartItm.inList, inCart = false)
                                }
                                .flatMap {
                                    updateShoppingListItem(token, ShoppingListItemUpdate(slid,
                                            it.id, inCart = false, categoryName = it.categoryName(),
                                            storeBranchName = branchName, storeName = storeName))
                                }
                                .toObservable()

                        obs = if (obs == null) {
                            currObs
                        } else {
                            Observable.concat(obs, currObs)
                        }
                    }

                    // 2. Collect and accumulate all new prices
                    return@flatMap Single.create<List<ShoppingListItem>> {
                        val rsltItems = mutableListOf<ShoppingListItem>()
                        obs!!
                                .doOnCompleted { it.onSuccess(rsltItems) }
                                .subscribe({ sli -> rsltItems.add(sli) }, it::onError)
                    }
                }
                .map { checkoutItems = it }

                .flatMap {
                    Single.create<ShoppingList> {
                        shoppingListRef
                                .get()
                                .addOnSuccessListener { ds ->
                                    it.onSuccess(ds.toObject(FirestoreShoppingList::class.java)!!
                                            .toShoppingList(ds.id))
                                }
                                .addOnFailureListener(it::onError)
                    }
                }
                .map { shoppingList = it }

                .toCompletable()
                .andThen(Completable.create {

                    val batch = db.batch()

                    val histRef = collShoppingHistories(token).document()
                    val hist = FirestoreShoppingHistory(date, shoppingList!!, branch!!)
                    val histItmsRef = collShoppingHistoryItems(token, histRef.id)
                    batch.set(histRef, hist)

                    checkoutItems!!.forEach { chckOutItm ->
                        val histItmRef = histItmsRef.document()
                        val histItm = FirestoreShoppingHistoryItem(chckOutItm)
                        batch.set(histItmRef, histItm)
                    }

                    val fsList = FirestoreShoppingList(shoppingList!!.name,
                            shoppingList!!.activeListPrice, shoppingList!!.cartPrice,
                            ShoppingMode.PREPARATION.name)
                    batch.set(shoppingListRef, fsList)

                    batch.commit()
                            .addOnSuccessListener { _ -> it.onCompleted() }
                            .addOnFailureListener { ex -> it.onError(ex) }
                })
    }

    override fun searchCategory(token: String, q: String): Single<List<Category>> {
        return Single.create<List<Category>> {
            collCategories.get()
                    .addOnSuccessListener { qs: QuerySnapshot ->
                        if (qs.isEmpty) {
                            it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                            return@addOnSuccessListener
                        }
                        val res = qs.documents
                                .map {
                                    it.toObject(Named::class.java)!!
                                            .toCategory(it.id)
                                }
                                .filter { it.name.toLowerCase().contains(q.toLowerCase()) }
                                .sortedBy { it.name }
                        it.onSuccess(res)
                    }
                    .addOnFailureListener { ex -> it.onError(ex) }
        }
    }

    override fun searchShoppingListItem(token: String, req: ShoppingListItemSearch): Single<List<ShoppingListItem>> {
        return Single.error(httpException(STATUS_NOT_FOUND, "not even implemented"))
    }

    private fun getShoppingList(tx: Transaction, ref: DocumentReference): ShoppingList {
        return tx.get(ref)
                .toObject(FirestoreShoppingList::class.java)!!
                .toShoppingList(ref.id)
    }

    private fun insertShoppingListItemIfNotExists(token: String, shoppingListID: String, cat: Category,
                                                  quantity: Int, inList: Boolean, inCart: Boolean,
                                                  price: BrandPrice): Single<ShoppingListItem> {

        var hasInserted = false
        return getShoppingListItemByPriceID(token, shoppingListID, price.id)
                .onErrorResumeNext { e: Throwable ->
                    if (!isNotFound(e)) {
                        return@onErrorResumeNext Single.error(e)
                    }
                    hasInserted = true
                    return@onErrorResumeNext insertShoppingListItem(token, shoppingListID, cat,
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

    private fun insertShoppingListItem(token: String, shoppingListID: String, cat: Category, quantity: Int,
                                       inList: Boolean, inCart: Boolean,
                                       price: BrandPrice) = Single.create<ShoppingListItem> {

        val doc = FirestoreShoppingListItem(quantity, inList, inCart, price, cat)
        val docRef = collShoppingListItems(token, shoppingListID).document()

        db
                .runTransaction { tx ->

                    val shoppingListRef = collShoppingLists(token).document(shoppingListID)
                    val shoppingList = tx.get(shoppingListRef)
                            .toObject(FirestoreShoppingList::class.java)!!
                            .toShoppingList(shoppingListID)
                            .accumulateInsertPrices(price.price * quantity, inList, inCart)
                    tx.set(shoppingListRef, FirestoreShoppingList(shoppingList))

                    tx.set(docRef, doc)

                    return@runTransaction null
                }
                .addOnSuccessListener { _ ->
                    it.onSuccess(doc.toShoppingListItem(docRef.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getShoppingListItem(token: String, shoppingListID: String, ID: String) = Single.create<ShoppingListItem> {
        collShoppingListItems(token, shoppingListID)
                .document(ID)
                .get()
                .addOnSuccessListener { ds: DocumentSnapshot ->
                    val fsListItem = ds.toObject(FirestoreShoppingListItem::class.java)!!
                    it.onSuccess(fsListItem.toShoppingListItem(ds.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getShoppingListItemByPriceID(token: String, shoppingListID: String, priceID: String) = Single.create<ShoppingListItem> {
        collShoppingListItems(token, shoppingListID)
                .whereEqualTo(FirestoreShoppingListItem.KEY_PRICE_ID, priceID)
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

    private fun insertPriceIfNotExists(price: Float, brand: Brand, branch: StoreBranch) =
            getPrice(price, brand.id, branch.id)
                    .onErrorResumeNext { e: Throwable ->
                        if (!isNotFound(e)) {
                            return@onErrorResumeNext Single.error(e)
                        }
                        return@onErrorResumeNext insertPrice(price, brand, branch)
                    }

    private fun insertPrice(price: Float, brand: Brand, branch: StoreBranch) = Single.create<BrandPrice> {
        val doc = FirestorePrice(price, brand, branch)
        collPrices
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toPrice(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getPrice(price: Float, brandID: String, branchID: String) = Single.create<BrandPrice> {
        collPrices
                .whereEqualTo(FirestorePrice.KEY_PRICE, price)
                .whereEqualTo(FirestorePrice.KEY_BRAND_ID, brandID)
                .whereEqualTo(FirestorePrice.KEY_BRANCH_ID, branchID)
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
                .whereEqualTo(FirestoreBrand.KEY_SHOPPING_ITEM_ID, itemID)
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

    private fun insertBranchIfNotExists(name: String, store: Store) =
            getBranch(name, store.id)
                    .onErrorResumeNext { e: Throwable ->
                        if (!isNotFound(e)) {
                            return@onErrorResumeNext Single.error(e)
                        }
                        return@onErrorResumeNext insertBranch(name, store)
                    }

    private fun insertBranch(name: String, store: Store) = Single.create<StoreBranch> {
        val doc = FirestoreBranch(name, store)
        collStoreBranches
                .add(doc)
                .addOnSuccessListener { dr: DocumentReference ->
                    it.onSuccess(doc.toBranch(dr.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun getBranch(name: String, storeID: String) = Single.create<StoreBranch> {
        collStoreBranches
                .whereEqualTo(FirestoreBranch.KEY_NAME, name)
                .whereEqualTo(FirestoreBranch.KEY_STORE_ID, storeID)
                .get()
                .addOnSuccessListener { qs: QuerySnapshot ->
                    if (qs.isEmpty) {
                        it.onError(httpException(STATUS_NOT_FOUND, "none found"))
                        return@addOnSuccessListener
                    }
                    val ss = qs.elementAt(0)
                    val fsBranch = ss.toObject(FirestoreBranch::class.java)
                    it.onSuccess(fsBranch.toBranch(ss.id))
                }
                .addOnFailureListener(it::onError)
    }

    private fun insertStoreIfNotExist(name: String) =
            insertNamedIfNotExists(collStores, name)

    private fun insertCategoryIfNotExist(name: String) =
            insertNamedIfNotExists(collCategories, name)

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

    private fun collShoppingHistories(token: String): CollectionReference {
        val jwt = jwtHelper.extractJWT(token)
        return db.collection(COLLECTION_USERS)
                .document(jwt.info.userID)
                .collection(COLLECTION_SHOPPING_HISTORIES)
    }

    private fun collShoppingHistoryItems(token: String, shoppingHistId: String): CollectionReference {
        return collShoppingHistories(token)
                .document(shoppingHistId)
                .collection(COLLECTION_SHOPPING_HISTORY_ITEMS)
    }

    companion object {

        const val COLLECTION_USERS = "users"
        const val COLLECTION_SHOPPING_LISTS = "shopping_lists"
        const val COLLECTION_SHOPPING_LIST_ITEMS = "shopping_list_items"
        const val COLLECTION_SHOPPING_HISTORIES = "shopping_histories"
        const val COLLECTION_SHOPPING_HISTORY_ITEMS = "shopping_history_items"
        const val COLLECTION_PRICES = "prices"
        const val COLLECTION_BRANDS = "brands"
        const val COLLECTION_MEASURING_UNITS = "measuring_units"
        const val COLLECTION_ITEMS = "items"
        const val COLLECTION_CATEGORIES = "categories"
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_STORE_BRANCHES = "store_branches"
    }

}

@Suppress("unused")
const val DOC_FIELD_SEPARATOR = "."

@Suppress("MemberVisibilityCanBePrivate")
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

    companion object {
        const val KEY_NAME = "name"
    }
}

@Suppress("MemberVisibilityCanBePrivate")
data class FirestoreShoppingListItem(
        val quantity: Int = 0,
        val inList: Boolean = false,
        val inCart: Boolean = false,
        val priceID: String = "",
        val categoryID: String = "",
        val price: FirestorePrice = FirestorePrice(),
        val category: Named = Named()
) {

    constructor(quantity: Int, inList: Boolean, inCart: Boolean, price: BrandPrice, cat: Category) :
            this(quantity, inList, inCart, price.id, cat.id, FirestorePrice(price), Named(cat.name))

    fun toShoppingListItem(id: String) =
            ShoppingListItem(id, quantity, price.toPrice(priceID), category.toCategory(categoryID),
                    inList, inCart)

    companion object {
        const val KEY_PRICE_ID = "priceId"
        const val KEY_IN_CART = "inCart"
        const val KEY_IN_LIST = "inList"
    }
}

data class FirestoreShoppingHistory(
        val checkoutDate: Date = Date(),
        val shoppingListID: String = "",
        val shoppingList: FirestoreShoppingList = FirestoreShoppingList(),
        val branchID: String = "",
        val branch: FirestoreBranch = FirestoreBranch()
) {
    constructor(checkoutDate: Date, sl: ShoppingList, br: StoreBranch) :
            this(checkoutDate, sl.id, FirestoreShoppingList(sl), br.id, FirestoreBranch(br))
}

data class FirestoreShoppingHistoryItem(
        val quantity: Int = 0,
        val priceID: String = "",
        val categoryID: String = "",
        val price: FirestorePrice = FirestorePrice(),
        val category: Named = Named()
) {

    constructor(item: ShoppingListItem) : this(item.quantity, item.brandPrice, item.category)

    constructor(quantity: Int, price: BrandPrice, cat: Category) :
            this(quantity, price.id, cat.id, FirestorePrice(price), Named(cat.name))
}

@Suppress("MemberVisibilityCanBePrivate")
data class FirestorePrice(
        val price: Float = 0F,
        val brandID: String = "",
        val brand: FirestoreBrand = FirestoreBrand(),
        val branchID: String = "",
        val branch: FirestoreBranch = FirestoreBranch()
) {

    constructor(price: BrandPrice) : this(price.price, price.brand, price.atStoreBranch)

    constructor(price: Float, brand: Brand, branch: StoreBranch) :
            this(price, brand.id, FirestoreBrand(brand), branch.id, FirestoreBranch(branch))

    fun toPrice(id: String) = BrandPrice(id, price, brand.toBrand(brandID), branch.toBranch(branchID))

    companion object {
        const val KEY_PRICE = "price"
        const val KEY_BRAND_ID = "brandId"
        const val KEY_BRANCH_ID = "branchId"
    }
}

@Suppress("MemberVisibilityCanBePrivate")
data class FirestoreBranch(
        val name: String = "",
        val storeID: String = "",
        val store: Named = Named()
) {
    constructor(branch: StoreBranch) : this(branch.name, branch.store)

    constructor(name: String, store: Store) : this(name, store.id, Named(store.name))

    fun toBranch(id: String) = StoreBranch(id, name, store.toStore(storeID))

    companion object {
        const val KEY_NAME = "name"
        const val KEY_STORE_ID = "storeId"
    }
}

@Suppress("MemberVisibilityCanBePrivate")
data class FirestoreBrand(
        val name: String = "",
        val measuringUnitID: String = "",
        val measuringUnit: Named = Named(),
        val shoppingItemID: String = "",
        val shoppingItem: Named = Named()
) {

    constructor(brand: Brand) : this(brand.name, brand.measuringUnit, brand.shoppingItem)

    constructor(name: String, measUnit: MeasuringUnit, item: ShoppingItem) :
            this(name, measUnit.id, Named(measUnit.name), item.id, Named(item.name))

    fun toBrand(id: String) = Brand(id, name, measuringUnit.toMeasuringUnit(measuringUnitID),
            shoppingItem.toShoppingItem(shoppingItemID))

    companion object {
        const val KEY_NAME = "name"
        const val KEY_SHOPPING_ITEM_ID = "shoppigItemId"
        const val KEY_MEASURING_UNIT_ID = "measuringUnitId"
    }
}

data class Named(val name: String = "") {

    fun toCategory(id: String) = Category(id, name)
    fun toShoppingItem(id: String) = ShoppingItem(id, name)
    fun toMeasuringUnit(id: String) = MeasuringUnit(id, name)
    fun toStore(id: String) = Store(id, name)

    companion object {
        const val KEY_NAME = "name"
    }
}