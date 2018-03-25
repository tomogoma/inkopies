package ke.co.definition.inkopies.model.stores

/**
 * Created by tomogoma
 * On 25/03/18.
 */
data class Store(
        val id: String,
        val name: String
)

data class StoreBranch(
        val id: String,
        val name: String,
        val store: Store
)