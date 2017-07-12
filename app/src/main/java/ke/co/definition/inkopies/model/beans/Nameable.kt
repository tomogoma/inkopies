package ke.co.definition.inkopies.model.beans

/**
 * Created by tomogoma on 10/07/17.
 */
open class Nameable : Profile() {
    open var name: String? = null

    override fun sanitize() {
        name = name?.trim()
    }
}
