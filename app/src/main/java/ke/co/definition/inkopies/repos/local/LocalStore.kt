package ke.co.definition.inkopies.repos.local

import android.app.Application
import android.content.Context
import javax.inject.Inject

/**
 * Created by tomogoma
 * On 28/02/18.
 */
class LocalStore @Inject constructor(val app: Application) : LocalStorable {

    override fun upsert(key: String, value: String) {
        val isSuccess = app.getSharedPreferences(KEY_SHARED_PREF_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .commit()
        if (!isSuccess) {
            throw RuntimeException("error commiting key/value to shared preference")
        }
    }

    override fun fetch(key: String) =
            app.getSharedPreferences(KEY_SHARED_PREF_FILE, Context.MODE_PRIVATE)
                    .getString(key, "")

    companion object {
        val KEY_SHARED_PREF_FILE = LocalStore::class.java.name + "SHARED_PREF_FILE"
    }
}