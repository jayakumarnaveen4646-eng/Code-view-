package com.example.data.repository

import com.example.data.db.LessonCompletionDao
import com.example.data.db.QuizHighScoreDao
import com.example.data.db.SavedCodeSnippetDao
import com.example.data.model.LessonCompletion
import com.example.data.model.QuizHighScore
import com.example.data.model.SavedCodeSnippet
import kotlinx.coroutines.flow.Flow

class TutorRepository(
    private val lessonDao: LessonCompletionDao,
    private val quizDao: QuizHighScoreDao,
    private val snippetDao: SavedCodeSnippetDao
) {
    // Lesson completion operations
    fun getCompletedLessons(language: String): Flow<List<LessonCompletion>> =
        lessonDao.getCompletedLessons(language)

    suspend fun saveLessonCompletion(language: String, lessonId: Int, isCompleted: Boolean) {
        lessonDao.insertOrUpdateCompletion(LessonCompletion(language, lessonId, isCompleted))
    }

    suspend fun clearCompletedLessons(language: String) {
        lessonDao.clearCompletedLessons(language)
    }

    // Quiz score operations
    fun getHighScore(language: String): Flow<QuizHighScore?> =
        quizDao.getHighScoreFlow(language)

    fun getAllHighScores(): Flow<List<QuizHighScore>> =
        quizDao.getAllHighScores()

    suspend fun saveHighScore(language: String, score: Int, totalQuestions: Int) {
        quizDao.insertOrUpdateHighScore(QuizHighScore(language, score, totalQuestions))
    }

    // Saved snippet operations
    fun getAllSnippets(): Flow<List<SavedCodeSnippet>> =
        snippetDao.getAllSnippets()

    fun getSnippetsByLanguage(language: String): Flow<List<SavedCodeSnippet>> =
        snippetDao.getSnippetsByLanguage(language)

    suspend fun saveSnippet(title: String, language: String, code: String) {
        snippetDao.insertSnippet(SavedCodeSnippet(title = title, language = language, code = code))
    }

    suspend fun deleteSnippet(snippet: SavedCodeSnippet) {
        snippetDao.deleteSnippet(snippet)
    }

    suspend fun deleteSnippetById(id: Int) {
        snippetDao.deleteSnippetById(id)
    }
}
