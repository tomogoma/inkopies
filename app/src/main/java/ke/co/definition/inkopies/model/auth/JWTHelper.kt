package ke.co.definition.inkopies.model.auth

import android.util.Base64
import com.google.gson.Gson
import java.nio.charset.Charset

/**
 * Created by tomogoma
 * On 03/04/18.
 */
interface JWTHelper {
    fun extractJWT(jwtStr: String): JWT
    fun extractInfo(value: String): JWTInfo
}

class JWTHelperImpl : JWTHelper {

    override fun extractJWT(jwtStr: String): JWT = JWT(jwtStr, extractInfo(jwtStr))

    override fun extractInfo(value: String): JWTInfo {
        val parts = value.split(".")
        if (parts.size != 3) {
            throw Exception("invalid JWT: has ${parts.size} parts instead of 3")
        }
        val jwtBytes = Base64.decode(parts[1], Base64.URL_SAFE)
        val jwtStr = String(jwtBytes, Charset.defaultCharset())
        return Gson().fromJson(jwtStr, JWTInfo::class.java)
    }
}