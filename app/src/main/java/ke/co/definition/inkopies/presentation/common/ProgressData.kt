package ke.co.definition.inkopies.presentation.common

/**
 * Created by tomogoma
 * On 02/03/18.
 */
data class ProgressData(
        val show: Boolean,
        val text: String
) {
    constructor(text: String) : this(true, text)
    constructor() : this(false, "")
}