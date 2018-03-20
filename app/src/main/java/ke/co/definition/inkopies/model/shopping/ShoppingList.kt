package ke.co.definition.inkopies.model.shopping

/**
 * Created by tomogoma
 * On 20/03/18.
 */
data class ShoppingList(
        val id: String,
        val name: String,
        val activeListPrice: Float,
        val cartPrice: Float
) {

    fun getFormattedActiveListPrice() = String.format("%,.2f", activeListPrice)
    fun getFormattedCartPrice() = String.format("%,.2f", cartPrice)
    fun isShowActiveListPrice() = activeListPrice > 0
    fun isShowCartPrice() = cartPrice > 0
}