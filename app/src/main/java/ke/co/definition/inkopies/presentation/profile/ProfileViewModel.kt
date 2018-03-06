package ke.co.definition.inkopies.presentation.profile

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import ke.co.definition.inkopies.model.user.UserProfile

/**
 * Created by tomogoma
 * On 06/03/18.
 */
class ProfileViewModel : ViewModel() {

    val userProfile = ObservableField<UserProfile>()
    val googleLink = ObservableField<Boolean>()
    val fbLink = ObservableField<Boolean>()
    val googleLinkText = ObservableField<String>()
    val fbLinkText = ObservableField<String>()
}