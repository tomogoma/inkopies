package ke.co.definition.inkopies.repos.room

import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update
import io.reactivex.Single

interface CrudDao<T> {

    @Insert(onConflict = REPLACE)
    fun insert(it: T): Single<Long>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg its: T): Single<LongArray>

    @Update
    fun update(it: T): Single<Int>

    @Update
    fun updateAll(vararg its: T): Single<Int>
}