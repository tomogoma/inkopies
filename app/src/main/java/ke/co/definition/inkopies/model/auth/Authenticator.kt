package ke.co.definition.inkopies.model.auth

import com.google.gson.Gson
import ke.co.definition.inkopies.repos.LocalStorable
import rx.Completable
import rx.Single
import java.util.*
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 28/02/18.
 */


class Authenticator @Inject constructor(val localStore: LocalStorable) : Authable {

    override fun isLoggedIn(): Single<Boolean> = Single.create({

        val loginStr = localStore.fetch(KEY_LOGIN_DETAILS)
        if (loginStr.isEmpty()) {
            it.onSuccess(false)
            return@create
        }

        val authUsr = Gson().fromJson(loginStr, AuthUser::class.java)
        if (authUsr.expiry.after(Date())) {
            it.onSuccess(false)
            return@create
        }

        it.onSuccess(true)
    })

    override fun registerManual(identifier: String, password: String) = Completable.create({
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    })

    override fun loginManual(identifier: String, password: String) = Completable.create({
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    })

    companion object {
        val KEY_LOGIN_DETAILS = Authenticator::class.java.name + "KEY_LOGIN_DETAILS"
    }

}
