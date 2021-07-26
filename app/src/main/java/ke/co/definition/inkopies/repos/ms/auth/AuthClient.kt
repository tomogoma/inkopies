package ke.co.definition.inkopies.repos.ms.auth

import io.reactivex.Completable
import io.reactivex.Single
import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.model.auth.OTPStatus

/**
 * Created by tomogoma
 * On 01/03/18.
 */
interface AuthClient {
    fun getUserID(id: Identifier): Single<String>
    fun registerManual(id: Identifier, secret: String): Single<Pair<AuthUser, String>>
    fun sendVerifyOTP(token: String, id: Identifier): Single<OTPStatus>
    fun login(id: Identifier, secret: String): Single<Pair<AuthUser, String>>
    fun fetchUserDetails(token: String, userID: String): Single<AuthUser>
    fun verifyOTP(userID: String, loginType: String, otp: String): Completable
    fun updateIdentifier(userID: String, token: String, id: Identifier): Single<AuthUser>
}