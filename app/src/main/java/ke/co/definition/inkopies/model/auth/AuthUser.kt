package ke.co.definition.inkopies.model.auth

import java.util.*

/**
 * Created by tomogoma
 * On 28/02/18.
 */
data class AuthUser(
        val id: String,
        val token: String,
        val expiry: Date,
        val phone: VerifLogin,
        val email: VerifLogin
)

data class VerifLogin(
        val id: String,
        val userID: String,
        val value: String,
        val verified: Boolean
)