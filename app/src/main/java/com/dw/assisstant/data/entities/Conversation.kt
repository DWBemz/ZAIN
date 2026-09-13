package com.dw.assisstant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String = "New conversation",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)