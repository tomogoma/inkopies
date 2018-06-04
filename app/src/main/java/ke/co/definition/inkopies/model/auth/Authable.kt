package ke.co.definition.inkopies.model.auth

import com.bumptech.glide.load.model.GlideUrl
import rx.Completable
import rx.Observable
import rx.Single

/**
 * Created by tomogoma
 * On 28/02/18.
 */
interface Authable {
    fun getUserID(id: Identifier): Single<String>
    fun isLoggedIn(): Single<LoggedInStatus>
    fun registerManual(id: Identifier, password: String): Single<VerifLogin>
    fun loginManual(id: Identifier, password: String): Single<LoggedInStatus>
    fun sendVerifyOTP(vl: VerifLogin): Single<OTPStatus>
    fun checkIdentifierVerified(vl: VerifLogin): Completable
    fun verifyOTP(vl: VerifLogin, otp: String?): Completable
    fun updateIdentifier(identifier: String): Single<VerifLogin>
    fun resendInterval(otps: OTPStatus?, intervalSecs: Long): Observable<Long>
    fun getUser(): Single<AuthUser>
    fun glideURL(url: String): Single<GlideUrl>
    fun getJWT(): Single<JWT>
    fun registerLoggedInStatusObserver(observer: (Boolean) -> Unit): Long
    fun unRegisterLoggedInStatusObserver(atPos: Long)
    fun logOut(): Completable
}