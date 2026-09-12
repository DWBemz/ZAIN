package com.dw.assisstant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dw.assisstant.data.entities.Skill

@Dao
interface SkillDao {

    @Insert
    suspend fun insert(skill: Skill): Long

    @Query("""
        SELECT * FROM skills
        WHERE enabled = 1
        ORDER BY name
    """)
    suspend fun getEnabled(): List<Skill>
}
