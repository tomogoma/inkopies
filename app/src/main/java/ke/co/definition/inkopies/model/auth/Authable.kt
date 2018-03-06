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
    fun sendVerifyOTP(vl: VerifLogin): Single<OTPStatus>
    fun checkIdentifierVerified(vl: VerifLogin): Completable
    fun verifyOTP(vl: VerifLogin, otp: String?): Completable
    fun updateIdentifier(identifier: String): Single<VerifLogin>
    fun resendInterval(otps: OTPStatus?, intervalSecs: Long): Observable<String>
}