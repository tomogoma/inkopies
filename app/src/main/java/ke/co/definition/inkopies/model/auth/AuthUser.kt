package ke.co.definition.inkopies.model.auth

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by tomogoma
 * On 28/02/18.
 */
data class AuthUser(
        @SerializedName("ID") val id: String,
        @SerializedName("JWT") val token: String,
        val expiry: Date,
        val phone: VerifLogin,
        val email: VerifLogin
)

data class VerifLogin(
        @SerializedName("ID") val id: String,
        val userID: String,
        val value: String,
        val verified: Boolean,
        @SerializedName("OTPStatus") val otpStatus: OTPStatus?
)

data class OTPStatus(
        val obfuscatedAddress: String,
        val expiresAt: Date
) {
    fun isExpired(): Boolean = expiresAt.before(Date())
}