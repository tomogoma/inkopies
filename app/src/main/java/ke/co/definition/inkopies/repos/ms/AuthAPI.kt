package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.model.auth.AuthUser
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path
import rx.Single

/**
 * Created by tomogoma
 * On 01/03/18.
 */
interface AuthAPI {

    @Headers("$HEADER_API_KEY: $API_KEY")
    @PUT("{loginType}/register?$KEY_SELF_REG=device")
    fun register(
            @Path("loginType") loginType: String,
            @Body body: AuthRegRequest
    ): Single<AuthUser>
}