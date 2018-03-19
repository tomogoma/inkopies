package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import retrofit2.adapter.rxjava.HttpException
import java.io.IOException

/**
 * Created by tomogoma
 * On 01/03/18.
 */

const val MFALME_ADDRESS = "https://definition.co.ke/gw/"

const val AUTH_MS_ADDRESS = MFALME_ADDRESS + "v0/authms/"
const val IMAGE_MS_ADDRESS = MFALME_ADDRESS + "v0/imagems/"
const val USERS_MS_ADDRESS = MFALME_ADDRESS + "v0/usersms/"

const val API_KEY = "vEftJGc9tWWk6a2tSMym7SYfWXPNNDMvjyEgHN3KrBpYqDRTCWatAx2g"

const val STATUS_BAD_REQUEST = 400
const val STATUS_UNAUTHORIZED = 401
const val STATUS_FORBIDDEN = 403
const val STATUS_NOT_FOUND = 404
const val STATUS_CONFLICT = 409
const val STATUS_SERVER_ERROR = 500

class LoggedOutException(msg: String) : Exception(msg)

fun handleAuthErrors(resMan: ResourceManager, err: Throwable, ctx: String = ""): Throwable {
    if (err is HttpException && err.code() == STATUS_FORBIDDEN) {
        return Exception(resMan.getString(R.string.forbidden_res_access))
    }
    if (err is HttpException && err.code() == STATUS_FORBIDDEN) {
        return LoggedOutException(resMan.getString(R.string.log_in_again))
    }
    return handleServerErrors(resMan, err, ctx)
}

fun handleServerErrors(resMan: ResourceManager, err: Throwable, ctx: String = ""): Throwable {
    if (err is HttpException && err.code() >= STATUS_SERVER_ERROR) {
        // TODO log WARN with error and ctx
        return Exception(resMan.getString(R.string.error_something_wicked))
    }
    if (err is IOException) {
        // TODO log WARN with error and ctx
        return Exception(resMan.getString(R.string.error_couldnt_reach_server))
    }
    // TODO log ERROR with error and ctx
    throw Exception(resMan.getString(R.string.error_something_wicked))
}