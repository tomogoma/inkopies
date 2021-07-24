package ke.co.definition.inkopies.repos.room

import androidx.room.*

@Entity(
        tableName = "shopping_lists",
        indices = [Index(value = ["name"], unique = true)]
)
data class ShoppingList(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "mode") val mode: String
)

@Entity(
        tableName = "categories",
        indices = [Index(value = ["name"], unique = true)]
)
data class Category(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "name") val name: String
)

@Entity(
        tableName = "shopping_list_item_names",
        foreignKeys = [
            ForeignKey(entity = Category::class,
                    parentColumns = ["rowid"],
                    childColumns = ["category_id"])
        ],
        indices = [Index(value = ["category_id"]),
            Index(value = ["name"], unique = true)]
)
data class ShoppingListItemName(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "category_id") val shoppingCategoryId: Int,
        @ColumnInfo(name = "name") val name: String
)

@Entity(
        tableName = "brands",
        indices = [Index(value = ["name"], unique = true)]
)
data class Brand(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "name") val name: String
)

@Entity(
        tableName = "stores",
        indices = [Index(value = ["name"], unique = true)]
)
data class Store(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "name") val name: String
)

@Entity(
        tableName = "store_branches",
        foreignKeys = [
            ForeignKey(entity = Store::class,
                    parentColumns = ["rowid"],
                    childColumns = ["store_id"])
        ],
        indices = [Index(value = ["store_id"]),
            Index(value = ["name"], unique = true)]
)
data class StoreBranch(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "store_id") val storeId: Int,
        @ColumnInfo(name = "name") val name: String
)

@Entity(
        tableName = "measurements",
        indices = [Index(value = ["name"], unique = true)]
)
data class Measurement(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "name") val name: String
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
                    childColumns = ["measurement_id"])
        ],
        indices = [
            Index(value = ["item_name_id"]),
            Index(value = ["brand_id"]),
            Index(value = ["measurement_id"]),
            Index(value = ["item_name_id", "brand_id", "measurement_id"], unique = true)
        ]
)
data class ItemBrandPrice(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "item_name_id") val itemNameId: Int,
        @ColumnInfo(name = "brand_id") val brandId: Int,
        @ColumnInfo(name = "measurement_id") val measurementId: Int,
        @ColumnInfo(name = "unit_price") val name: String
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
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "store_branch_id") val storeBranchId: Int,
        @ColumnInfo(name = "shopping_list_id") val shoppingListId: Int,
        @ColumnInfo(name = "date_time") val dateTime: Long
)

@Entity(
        tableName = "shopping_list_items",
        foreignKeys = [
            ForeignKey(entity = ShoppingList::class,
                    parentColumns = ["rowid"],
                    childColumns = ["shopping_list_id"]),
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
            Index(value = ["shopping_list_item_name_id"]),
            Index(value = ["brand_id"]),
            Index(value = ["measurement_id"]),
            Index(value = ["shopping_list_id", "shopping_list_item_name_id", "brand_id", "measurement_id"], unique = true)
        ]
)
data class ShoppingListItem(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "shopping_list_id") val shoppingListId: Int,
        @ColumnInfo(name = "shopping_list_item_name_id") val shoppingListItemId: Int,
        @ColumnInfo(name = "brand_id") val brandId: Int,
        @ColumnInfo(name = "measurement_id") val measurementId: Int,
        @ColumnInfo(name = "in_list") val inList: Boolean,
        @ColumnInfo(name = "in_cart") val inCart: Boolean,
        @ColumnInfo(name = "quantity") val quantity: Int
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
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
        @ColumnInfo(name = "checkout_id") val checkoutId: Int,
        @ColumnInfo(name = "shopping_list_item_name_id") val shoppingListItemId: Int,
        @ColumnInfo(name = "brand_id") val brandId: Int,
        @ColumnInfo(name = "measurement_id") val measurementId: Int,
        @ColumnInfo(name = "quantity") val quantity: Int,
        @ColumnInfo(name = "effective_item_name") val effectiveItemName: Int,
        @ColumnInfo(name = "effective_brand_name") val effectiveBrandName: Int,
        @ColumnInfo(name = "effective_measurement") val effectiveMeasurement: Int,
        @ColumnInfo(name = "effective_unit_price") val unitPrice: Int
)



