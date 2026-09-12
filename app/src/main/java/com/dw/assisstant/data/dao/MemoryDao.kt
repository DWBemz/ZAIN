package com.dw.assisstant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dw.assisstant.data.entities.Memory

@Dao
interface MemoryDao {

    @Insert
    suspend fun insert(memory: Memory): Long

    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    suspend fun getAll(): List<Memory>

    @Query("""
        SELECT * FROM memories
        WHERE content LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    suspend fun search(query: String): List<Memory>

    @Query("""
        SELECT * FROM memories
        WHERE category = :category
        ORDER BY updatedAt DESC
    """)
    suspend fun getByCategory(category: String): List<Memory>

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun count(): Int
}