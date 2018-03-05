package ke.co.definition.inkopies.model

import android.app.Application
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 05/03/18.
 */
class ResourceManagerImpl @Inject constructor(private val app: Application) : ResourceManager {
    override fun getString(resID: Int): String = app.getString(resID)

}