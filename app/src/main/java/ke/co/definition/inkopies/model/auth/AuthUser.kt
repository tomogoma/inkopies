package ke.co.definition.inkopies.model.auth

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by tomogoma
 * On 28/02/18.
 */
data class AuthUser(
        val id: String,
        val phone: VerifLogin,
        val email: VerifLogin
)

data class VerifLogin(
        val id: String = "",
        val userID: String = "",
        val value: String = "",
        val verified: Boolean = false,
        val otpStatus: OTPStatus? = null
)

data class OTPStatus(
        val obfuscatedAddress: String,
        val expiresAt: Date
) {
    fun isExpired(): Boolean = expiresAt.before(Date())
}

data class JWT(val value: String, val info: JWTInfo) {
    fun isExpired() = info.isExpired()
}

data class JWTInfo(
        @SerializedName("UsrID") val userID: String,
        @SerializedName("exp") private val expiresAt: Long
) {
    fun isExpired(): Boolean = Date(expiresAt * 1000).before(Date())
}