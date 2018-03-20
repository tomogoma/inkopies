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
        val avatarURL: String
) {

    constructor(auth: AuthUser, genUsr: GenUserProfile) :
            this(auth, genUsr.name, genUsr.gender, genUsr.avatarURL)

    fun getHumanGender(): String = if (gender == Gender.NONE) "" else gender.name.toLowerCase().capitalize()
}

data class GenUserProfile(
        val name: String = "",
        val gender: Gender = Gender.NONE,
        val avatarURL: String = ""
)

enum class Gender {
    NONE, MALE, FEMALE, OTHER
}