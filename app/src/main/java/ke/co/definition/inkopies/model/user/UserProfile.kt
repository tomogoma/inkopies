package ke.co.definition.inkopies.model.user

import ke.co.definition.inkopies.model.auth.AuthUser

/**
 * Created by tomogoma
 * On 06/03/18.
 */
data class UserProfile(
        val auth: AuthUser,
        val name: String,
        val imageURL: String
)