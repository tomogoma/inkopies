package ke.co.definition.inkopies.model.user

import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.auth.AuthUser
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.JWT
import ke.co.definition.inkopies.repos.ms.STATUS_NOT_FOUND
import ke.co.definition.inkopies.repos.ms.handleAuthErrors
import ke.co.definition.inkopies.repos.ms.image.ImageClient
import ke.co.definition.inkopies.repos.ms.users.UsersClient
import ke.co.definition.inkopies.utils.logging.Logger
import retrofit2.adapter.rxjava.HttpException
import rx.Completable
import rx.Single
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 09/03/18.
 */

const val PROFILE_IMG_FOLDER = "profile_images"

class ProfileManagerImpl @Inject constructor(
        private val resMan: ResourceManager,
        private val auth: Authable,
        private val usersCl: UsersClient,
        private val imageCl: ImageClient,
        private val logger: Logger
) : ProfileManager {

    init {
        logger.setTag(ProfileManagerImpl::class.java.name)
    }

    override fun getUser(): Single<UserProfile> {
        var authUser: AuthUser? = null
        return auth.getUser()
                .map { authUser = it }
                .flatMap { auth.getJWT() }
                .flatMap { usersCl.getUser(it.value, it.info.userID) }
                .onErrorResumeNext {
                    if (it is HttpException && it.code() == STATUS_NOT_FOUND) {
                        return@onErrorResumeNext Single.just(GenUserProfile())
                    }
                    return@onErrorResumeNext Single.error(handleAuthErrors(logger, resMan, it,
                            "get user"))
                }
                .flatMap { Single.just(UserProfile(authUser!!, it)) }
    }

    override fun updateGeneral(name: String, gender: Gender): Single<UserProfile> {
        var authUsr: AuthUser? = null
        return validateGeneral(name, gender).toSingle {}
                .flatMap { auth.getUser() }
                .map { authUsr = it }
                .flatMap { auth.getJWT() }
                .flatMap { usersCl.updateUser(it.value, it.info.userID, name, gender) }
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, resMan, it,
                            "update general profile"))
                }
                .flatMap { Single.just(UserProfile(authUsr!!, it)) }
    }

    override fun uploadProfilePic(uri: String): Single<UserProfile> {
        var authUsr: AuthUser? = null
        var jwt: JWT? = null
        return Completable.create {
            if (uri == "") {
                it.onError(Exception("Profile picture was not found"))
                return@create
            }
            it.onCompleted()
        }.toSingle {}
                .flatMap { auth.getUser() }
                .map { authUsr = it }
                .flatMap { auth.getJWT() }
                .flatMap { jwt = it;imageCl.uploadProfilePic(it.value, PROFILE_IMG_FOLDER, uri) }
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, resMan, it,
                            "upload profile picture"))
                }
                .flatMap { usersCl.updateAvatar(jwt!!.value, jwt!!.info.userID, it) }
                .onErrorResumeNext {
                    Single.error(handleAuthErrors(logger, resMan, it,
                            "update user's avatar URL"))
                }
                .flatMap { Single.just(UserProfile(authUsr!!, it)) }
    }

    private fun validateGeneral(name: String, gender: Gender) = Completable.create {
        if (name == "") {
            it.onError(Exception("name was empty"))
            return@create
        }
        if (gender == Gender.NONE) {
            it.onError(Exception("invalid gender provided"))
        }
        it.onCompleted()
    }

}