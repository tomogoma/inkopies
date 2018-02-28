package ke.co.definition.inkopies.repos

/**
 * Created by tomogoma
 * On 28/02/18.
 */
interface LocalStorable {
    fun upsert(key: String, value: String)
    fun fetch(key: String): String
}