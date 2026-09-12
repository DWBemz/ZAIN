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
}
