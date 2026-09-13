package com.dw.assisstant.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dw.assisstant.data.entities.Message

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: Message): Long

    @Query("""
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
    """)
    suspend fun getForConversation(
        conversationId: Long
    ): List<Message>

    @Query("""
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentMessages(
        conversationId: Long,
        limit: Int
    ): List<Message>

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(
        conversationId: Long
    )

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun countForConversation(
        conversationId: Long
    ): Int
}