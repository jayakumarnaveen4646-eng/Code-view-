package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_completion", primaryKeys = ["language", "lessonId"])
data class LessonCompletion(
    val language: String,
    val lessonId: Int,
    val isCompleted: Boolean = true
)

@Entity(tableName = "quiz_high_score")
data class QuizHighScore(
    @PrimaryKey val language: String,
    val highScore: Int,
    val totalQuestions: Int
)

@Entity(tableName = "saved_code_snippet")
data class SavedCodeSnippet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val language: String,
    val code: String,
    val timestamp: Long = System.currentTimeMillis()
)
