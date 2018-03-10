package ke.co.definition.inkopies.model.user

import ke.co.definition.inkopies.model.auth.Authable
import rx.Single
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 09/03/18.
 */
class ProfileManagerImpl @Inject constructor(
        private val auth: Authable
) : ProfileManager {
    override fun getUser(): Single<UserProfile> {
        return auth.getUser().flatMap {
            Single.just<UserProfile>(UserProfile(
                            auth = it,
                            name = "John Doe",
                            gender = Gender.MALE,
                            imageURL = "https://www.youthfit.com/hubfs/01-Althea_New_Pages/Blog/pbQi_VV3.png"
                    )
            )
        }
    }

    override fun updateGeneral(name: String, gender: Gender): Single<UserProfile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadProfilePic(uri: String): Single<UserProfile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateIdentifier(identifier: String): Single<UserProfile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}