package ke.co.definition.inkopies.model.user

import ke.co.definition.inkopies.model.auth.AuthUser

/**
 * Created by tomogoma
 * On 06/03/18.
 */
data class UserProfile(
        @Transient val auth: AuthUser,
        val name: String,
        val gender: Gender,
        val imageURL: String
) {
    fun getHumanGender(): String = gender.name.toLowerCase().capitalize()
}

enum class Gender {
    MALE, FEMALE, OTHER
}