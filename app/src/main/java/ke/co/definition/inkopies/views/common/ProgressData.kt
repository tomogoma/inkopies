package ke.co.definition.inkopies.views.common

/**
 * Created by tomogoma
 * On 02/03/18.
 */
data class ProgressData(
        val show: Boolean,
        val text: String
) {
    constructor() : this(false, "")
}