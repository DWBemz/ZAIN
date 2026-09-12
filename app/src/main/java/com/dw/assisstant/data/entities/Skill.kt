package com.dw.assisstant.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val description: String,

    val enabled: Boolean = true
)
