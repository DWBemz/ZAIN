package com.dw.assisstant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dw.assisstant.data.entities.Preference

@Dao
interface PreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(preference: Preference)

    @Query("""
        SELECT * FROM preferences
        WHERE `key` = :key
        LIMIT 1
    """)
    suspend fun get(key: String): Preference?
}
