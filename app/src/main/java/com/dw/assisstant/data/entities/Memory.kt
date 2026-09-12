package com.dw.assisstant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class Memory(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val content: String,

    val category: String = "general",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
