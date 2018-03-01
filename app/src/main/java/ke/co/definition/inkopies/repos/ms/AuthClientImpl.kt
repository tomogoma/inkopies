package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Identifier
import ke.co.definition.inkopies.utils.injection.AuthModule
import retrofit2.Retrofit
import rx.Single
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 01/03/18.
 */
class AuthClientImpl @Inject constructor(@Named(AuthModule.NAME) private val retrofit: Retrofit) : AuthClient {

    override fun registerManual(id: Identifier, secret: String): Single<AuthUser> =
            retrofit.create(AuthAPI::class.java)
                    .register(id.type(), AuthRegRequest(id.value(), secret))
}