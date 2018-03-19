package ke.co.definition.inkopies.repos.ms.auth

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.model.auth.OTPStatus
import rx.Completable
import rx.Single

/**
 * Created by tomogoma
 * On 01/03/18.
 */
interface AuthClient {
    fun registerManual(id: Identifier, secret: String): Single<AuthUser>
    fun sendVerifyOTP(token: String, id: Identifier): Single<OTPStatus>
    fun login(id: Identifier, secret: String): Single<AuthUser>
    fun fetchUserDetails(token: String, userID: String): Single<AuthUser>
    fun verifyOTP(userID: String, loginType: String, otp: String): Completable
    fun updateIdentifier(userID: String, token: String, id: Identifier): Single<AuthUser>
}