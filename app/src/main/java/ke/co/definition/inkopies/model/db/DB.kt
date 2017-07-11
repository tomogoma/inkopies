package ke.co.definition.inkopies.model.db

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Created by tomogoma on 08/07/17.
 */

@Database(name = DB.NAME, version = DB.VERSION, foreignKeyConstraintsEnforced = true)
class DB {
    companion object {
        const val NAME = "InkopiesDB"
        const val VERSION = 17
    }
}
