package ke.co.definition.inkopies.repos.room

import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Update
import io.reactivex.Single

interface UpdateDao<T> {
    @Update
    fun update(it: T): Single<Int>

    @Update
    fun updateAll(vararg its: T): Single<Int>
}

interface InsertWithIgnoreConflictDao<T> {
    @Insert(onConflict = IGNORE)
    fun insert(it: T): Single<Long>

    @Insert(onConflict = IGNORE)
    fun insertAll(vararg its: T): Single<LongArray>
}

interface InsertWithReplaceOnConflictDao<T> {
    @Insert(onConflict = REPLACE)
    fun insert(it: T): Single<Long>

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg its: T): Single<LongArray>
}

interface InsertDao<T> {
    fun insert(it: T): Single<Long>

    fun insertAll(vararg its: T): Single<LongArray>
}

interface CrudDao<T> :
        UpdateDao<T>,
        InsertDao<T>

interface CrudWithIgnoreConflictDao<T> :
        UpdateDao<T>,
        InsertWithIgnoreConflictDao<T>

interface CrudWithReplaceOnConflictDao<T> :
        UpdateDao<T>,
        InsertWithReplaceOnConflictDao<T>