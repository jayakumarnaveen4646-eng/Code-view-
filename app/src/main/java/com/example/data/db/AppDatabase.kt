package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.LessonCompletion
import com.example.data.model.QuizHighScore
import com.example.data.model.SavedCodeSnippet

@Database(
    entities = [
        LessonCompletion::class,
        QuizHighScore::class,
        SavedCodeSnippet::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonCompletionDao(): LessonCompletionDao
    abstract fun quizHighScoreDao(): QuizHighScoreDao
    abstract fun savedCodeSnippetDao(): SavedCodeSnippetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codemaster_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
