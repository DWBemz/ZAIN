package com.dw.assisstant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dw.assisstant.data.dao.ConversationDao
import com.dw.assisstant.data.dao.KnowledgeDao
import com.dw.assisstant.data.dao.MemoryDao
import com.dw.assisstant.data.dao.MessageDao
import com.dw.assisstant.data.dao.PreferenceDao
import com.dw.assisstant.data.dao.SkillDao
import com.dw.assisstant.data.entities.Conversation
import com.dw.assisstant.data.entities.Knowledge
import com.dw.assisstant.data.entities.Memory
import com.dw.assisstant.data.entities.Message
import com.dw.assisstant.data.entities.Preference
import com.dw.assisstant.data.entities.Skill

@Database(
    entities = [
        Memory::class,
        Conversation::class,
        Message::class,
        Knowledge::class,
        Preference::class,
        Skill::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZainDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    abstract fun knowledgeDao(): KnowledgeDao

    abstract fun preferenceDao(): PreferenceDao

    abstract fun skillDao(): SkillDao

    companion object {

        @Volatile
        private var INSTANCE: ZainDatabase? = null

        fun getInstance(context: Context): ZainDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZainDatabase::class.java,
                    "zain_local_brain.db"
                )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}