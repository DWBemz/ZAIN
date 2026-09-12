package com.dw.assisstant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dw.assisstant.data.entities.Knowledge

@Dao
interface KnowledgeDao {

    @Insert
    suspend fun insert(knowledge: Knowledge): Long

    @Query("SELECT * FROM knowledge ORDER BY updatedAt DESC")
    suspend fun getAll(): List<Knowledge>

    @Query("""
        SELECT * FROM knowledge
        WHERE topic LIKE '%' || :query || '%'
        OR content LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    suspend fun search(query: String): List<Knowledge>
}
