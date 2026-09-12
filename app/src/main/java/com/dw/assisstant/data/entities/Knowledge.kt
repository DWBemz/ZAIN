package com.dw.assisstant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge")
data class Knowledge(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val topic: String,

    val content: String,

    val source: String = "local",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
