package ke.co.definition.inkopies.model.auth

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.nio.charset.Charset
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

data class JWT(val value: String) {

    val info: JWTInfo

    init {
        val parts = value.split(".")
        if (parts.size != 3) {
            throw Exception("invalid JWT: has ${parts.size} parts instead of 3")
        }
        val jwtBytes = Base64.decode(parts[1], Base64.URL_SAFE)
        val jwtStr = String(jwtBytes, Charset.defaultCharset())
        info = Gson().fromJson(jwtStr, JWTInfo::class.java)
    }

    fun isExpired() = info.isExpired()
}

data class JWTInfo(
        @SerializedName("UsrID") val userID: String,
        @SerializedName("exp") private val expiresAt: Long
) {
    fun isExpired(): Boolean = Date(expiresAt * 1000).before(Date())
}