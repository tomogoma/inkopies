package ke.co.definition.inkopies.repos.ms

import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.utils.logging.Logger
import retrofit2.adapter.rxjava.HttpException
import java.io.IOException

/**
 * Created by tomogoma
 * On 01/03/18.
 */

const val STATUS_BAD_REQUEST = 400
const val STATUS_UNAUTHORIZED = 401
const val STATUS_FORBIDDEN = 403
const val STATUS_NOT_FOUND = 404
const val STATUS_CONFLICT = 409
const val STATUS_SERVER_ERROR = 500

class LoggedOutException(msg: String) : Exception(msg)

fun handleAuthErrors(lg: Logger, auth: Authable, resMan: ResourceManager, err: Throwable, ctx: String = ""): Throwable {

    if (err is HttpException && err.code() == STATUS_UNAUTHORIZED) {
        return Exception(resMan.getString(R.string.forbidden_res_access))
    }

    if (err is LoggedOutException) {
        return err
    }

    var rslt: Throwable? = null
    if (err is HttpException && err.code() == STATUS_FORBIDDEN) {

        auth.logOut().subscribe({
            rslt = LoggedOutException(resMan.getString(R.string.log_in_again))
        }, { rslt = it })

        while (rslt == null) {
            Thread.sleep(50)
        }
        if (rslt is LoggedOutException) {
            return rslt!!
        }
    }

    return handleServerErrors(lg, resMan,
            rslt ?: handleServerErrors(lg, resMan, err, ctx),
            ctx)
}

fun handleServerErrors(lg: Logger, resMan: ResourceManager, err: Throwable, ctx: String = ""): Throwable {

    if (err is HttpException) {

        if (err.code() >= STATUS_SERVER_ERROR) {
            lg.warn(String.format("Got server error during \"%s\": %s: %s",
                    ctx, err.message(), err.response()?.errorBody()?.string()))
            return Exception(resMan.getString(R.string.error_something_wicked))
        }

        lg.error(String.format("Got unchecked http error code(%d) \"%s\": %s",
                err.code(), ctx, err.response()?.errorBody()?.string()), err)
        return Exception(resMan.getString(R.string.error_something_wicked))
    }

    if (err is IOException) {
        lg.warn(String.format("Unable to reach server \"%s\": %s", ctx, err.message))
        return Exception(resMan.getString(R.string.error_couldnt_reach_server))
    }

    lg.error(String.format("Got unknown error \"%s\"", ctx), err)
    return Exception(resMan.getString(R.string.error_something_wicked))
}