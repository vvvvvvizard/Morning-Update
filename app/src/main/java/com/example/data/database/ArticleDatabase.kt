package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ArticleEntity
import com.example.data.model.DigestEntity
import com.example.data.model.EmailEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.YouTubeVideoEntity

@Database(
    entities = [ArticleEntity::class, DigestEntity::class, EmailEntity::class, FeedbackEntity::class, YouTubeVideoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun dao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null

        fun getDatabase(context: Context): ArticleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArticleDatabase::class.java,
                    "morning_digest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
