package ke.co.definition.inkopies.repos.ms

import java.util.*

/**
 * Created by tomogoma
 * On 01/03/18.
 */
data class AuthRegRequest(
        val identifier: String,
        val secret: String,
        val userType: String = "individual",
        val deviceID: String = UUID.randomUUID().toString()
)