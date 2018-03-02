package ke.co.definition.inkopies.model.auth

import rx.Completable
import rx.Observable
import rx.Single

/**
 * Created by tomogoma
 * On 28/02/18.
 */
interface Authable {
    fun isLoggedIn(): Single<Boolean>
    fun registerManual(id: Identifier, password: String): Single<VerifLogin>
    fun loginManual(id: Identifier, password: String): Completable
    fun sendVerifyOTP(id: Identifier): Single<OTPStatus>
    fun identifierVerified(vl: VerifLogin): Completable
    fun verifyOTP(vl: VerifLogin): Completable
    fun resendInterval(otps: OTPStatus, intervalSecs: Long): Observable<String>
}