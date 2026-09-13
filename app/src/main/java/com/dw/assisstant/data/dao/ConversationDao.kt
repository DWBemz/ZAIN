package com.dw.assisstant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dw.assisstant.data.entities.Conversation

@Dao
interface ConversationDao {

    @Insert
    suspend fun insert(conversation: Conversation): Long

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    suspend fun getAll(): List<Conversation>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getById(conversationId: Long): Conversation?

    @Query("UPDATE conversations SET updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun updateTimestamp(
        conversationId: Long,
        updatedAt: Long
    )

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun delete(conversationId: Long)
}