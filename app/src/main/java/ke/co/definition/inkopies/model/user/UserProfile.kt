package ke.co.definition.inkopies.model.user

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.repos.ms.users.MSUserProfile

/**
 * Created by tomogoma
 * On 06/03/18.
 */
data class UserProfile(
        @Transient val auth: AuthUser,
        val name: String,
        val gender: Gender,
        val avatarURL: String
) {

    constructor(auth: AuthUser, msUsr: MSUserProfile) :
            this(auth, msUsr.name, msUsr.gender, msUsr.avatarURL)

    fun getHumanGender(): String = if (gender == Gender.NONE) "" else gender.name.toLowerCase().capitalize()
}

enum class Gender {
    NONE, MALE, FEMALE, OTHER
}