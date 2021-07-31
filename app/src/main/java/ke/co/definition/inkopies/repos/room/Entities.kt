package ke.co.definition.inkopies.repos.room

import androidx.room.*

@Entity(
        tableName = "shopping_lists",
        indices = [Index(value = ["name"], unique = true)]
)
data class ShoppingList(
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "mode") val mode: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "categories",
        indices = [Index(value = ["name"], unique = true)]
)
data class Category(
        @ColumnInfo(name = "name") val name: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "shopping_list_item_names",
        indices = [Index(value = ["name"], unique = true)]
)
data class ShoppingListItemName(
        @ColumnInfo(name = "name") val name: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "brands",
        indices = [Index(value = ["name"], unique = true)]
)
data class Brand(
        @ColumnInfo(name = "name") val name: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "stores",
        indices = [Index(value = ["name"])]
)
data class Store(
        @ColumnInfo(name = "name") val name: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "store_branches",
        foreignKeys = [
            ForeignKey(entity = Store::class,
                    parentColumns = ["rowid"],
                    childColumns = ["store_id"])
        ],
        indices = [Index(value = ["store_id"]),
            Index(value = ["name"])]
)
data class StoreBranch(
        @ColumnInfo(name = "store_id") val storeId: Long,
        @ColumnInfo(name = "name") val name: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "measurements",
        indices = [Index(value = ["name"], unique = true)]
)
data class Measurement(
        @ColumnInfo(name = "name") val name: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "brand_prices",
        foreignKeys = [
            ForeignKey(entity = ShoppingListItemName::class,
                    parentColumns = ["rowid"],
                    childColumns = ["item_name_id"]),
            ForeignKey(entity = Brand::class,
                    parentColumns = ["rowid"],
                    childColumns = ["brand_id"]),
            ForeignKey(entity = Measurement::class,
                    parentColumns = ["rowid"],
                    childColumns = ["measurement_id"]),
            ForeignKey(entity = Measurement::class,
                    parentColumns = ["rowid"],
                    childColumns = ["store_branch_id"])
        ],
        indices = [
            Index(value = ["item_name_id"]),
            Index(value = ["brand_id"]),
            Index(value = ["measurement_id"]),
            Index(value = ["store_branch_id"])
        ]
)
data class Price(
        @ColumnInfo(name = "item_name_id") val itemNameId: Long,
        @ColumnInfo(name = "unit_price") val unitPrice: Float,
        @ColumnInfo(name = "brand_id") val brandId: Long? = null,
        @ColumnInfo(name = "measurement_id") val measurementId: Long? = null,
        @ColumnInfo(name = "store_branch_id") val storeBranchId: Long? = null,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "checkouts",
        foreignKeys = [
            ForeignKey(entity = StoreBranch::class,
                    parentColumns = ["rowid"],
                    childColumns = ["store_branch_id"]),
            ForeignKey(entity = ShoppingList::class,
                    parentColumns = ["rowid"],
                    childColumns = ["shopping_list_id"])
        ],
        indices = [
            Index(value = ["store_branch_id"]),
            Index(value = ["shopping_list_id"]),
            Index(value = ["store_branch_id", "shopping_list_id", "date_time"], unique = true)
        ]
)
data class Checkout(
        @ColumnInfo(name = "store_branch_id") val storeBranchId: Long,
        @ColumnInfo(name = "shopping_list_id") val shoppingListId: Long,
        @ColumnInfo(name = "date_time") val dateTime: Long,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "shopping_list_items",
        foreignKeys = [
            ForeignKey(entity = ShoppingList::class,
                    parentColumns = ["rowid"],
                    childColumns = ["shopping_list_id"]),
            ForeignKey(entity = Category::class,
                    parentColumns = ["rowid"],
                    childColumns = ["category_id"]),
            ForeignKey(entity = ShoppingListItemName::class,
                    parentColumns = ["rowid"],
                    childColumns = ["shopping_list_item_name_id"]),
            ForeignKey(entity = Brand::class,
                    parentColumns = ["rowid"],
                    childColumns = ["brand_id"]),
            ForeignKey(entity = Measurement::class,
                    parentColumns = ["rowid"],
                    childColumns = ["measurement_id"])
        ],
        indices = [
            Index(value = ["shopping_list_id"]),
            Index(value = ["category_id"]),
            Index(value = ["shopping_list_item_name_id"]),
            Index(value = ["brand_id"]),
            Index(value = ["measurement_id"]),
            Index(value = ["shopping_list_id", "shopping_list_item_name_id", "brand_id", "measurement_id"], unique = true)
        ]
)
data class ShoppingListItem(
        @ColumnInfo(name = "shopping_list_id") val shoppingListId: Long,
        @ColumnInfo(name = "shopping_list_item_name_id") val shoppingListItemNameId: Long,
        @ColumnInfo(name = "in_list") val inList: Boolean,
        @ColumnInfo(name = "in_cart") val inCart: Boolean,
        @ColumnInfo(name = "quantity") val quantity: Int?,
        @ColumnInfo(name = "category_id") val shoppingCategoryId: Long? = null,
        @ColumnInfo(name = "brand_id") val brandId: Long? = null,
        @ColumnInfo(name = "measurement_id") val measurementId: Long? = null,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)

@Entity(
        tableName = "checkout_items",
        foreignKeys = [
            ForeignKey(entity = Checkout::class,
                    parentColumns = ["rowid"],
                    childColumns = ["checkout_id"]),
            ForeignKey(entity = ShoppingListItemName::class,
                    parentColumns = ["rowid"],
                    childColumns = ["shopping_list_item_name_id"]),
            ForeignKey(entity = Brand::class,
                    parentColumns = ["rowid"],
                    childColumns = ["brand_id"]),
            ForeignKey(entity = Measurement::class,
                    parentColumns = ["rowid"],
                    childColumns = ["measurement_id"])
        ],
        indices = [
            Index(value = ["checkout_id"]),
            Index(value = ["shopping_list_item_name_id"]),
            Index(value = ["brand_id"]),
            Index(value = ["measurement_id"])
        ]
)
data class CheckoutItem(
        @ColumnInfo(name = "checkout_id") val checkoutId: Long,
        @ColumnInfo(name = "shopping_list_item_name_id") val shoppingListItemId: Long,
        @ColumnInfo(name = "brand_id") val brandId: Long,
        @ColumnInfo(name = "measurement_id") val measurementId: Long,
        @ColumnInfo(name = "quantity") val quantity: Int,
        @ColumnInfo(name = "effective_item_name") val effectiveItemName: Int,
        @ColumnInfo(name = "effective_brand_name") val effectiveBrandName: Int,
        @ColumnInfo(name = "effective_measurement") val effectiveMeasurement: Int,
        @ColumnInfo(name = "effective_unit_price") val unitPrice: Int,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int? = null
)



