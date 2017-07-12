package ke.co.definition.inkopies.model.beans

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Unique
import com.raizlabs.android.dbflow.structure.BaseModel
import java.io.Serializable
import java.util.*

/**
 * Created by tomogoma on 08/07/17.
 */

open class Profile : BaseModel(), Serializable {
    @PrimaryKey @Unique var serverID: String? = UUID.randomUUID().toString()
    @PrimaryKey var id: UUID? = UUID.randomUUID()
    @Column var createDate: Date? = Date()
    @Column var updateDate: Date? = Date()

    fun inheritIdentification(from: Profile) {
        serverID = from.serverID
        id = from.id
        createDate = from.createDate
        updateDate = Date()
    }

    open fun sanitize() = Unit
}
