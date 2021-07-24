package ke.co.definition.inkopies.repos.room

import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update
import rx.Single

interface CrudDao<T> {

    @Insert(onConflict = REPLACE)
    fun insert(it: T): Long

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg its: T): LongArray

    @Update
    fun update(it: T): Int

    @Update
    fun updateAll(vararg its: T): Int
}