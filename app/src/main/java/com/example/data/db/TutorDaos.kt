package com.example.data.db

import androidx.room.*
import com.example.data.model.LessonCompletion
import com.example.data.model.QuizHighScore
import com.example.data.model.SavedCodeSnippet
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonCompletionDao {
    @Query("SELECT * FROM lesson_completion WHERE language = :language")
    fun getCompletedLessons(language: String): Flow<List<LessonCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompletion(completion: LessonCompletion)

    @Query("DELETE FROM lesson_completion WHERE language = :language")
    suspend fun clearCompletedLessons(language: String)
}

@Dao
interface QuizHighScoreDao {
    @Query("SELECT * FROM quiz_high_score WHERE language = :language")
    fun getHighScoreFlow(language: String): Flow<QuizHighScore?>

    @Query("SELECT * FROM quiz_high_score")
    fun getAllHighScores(): Flow<List<QuizHighScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHighScore(highScore: QuizHighScore)
}

@Dao
interface SavedCodeSnippetDao {
    @Query("SELECT * FROM saved_code_snippet ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<SavedCodeSnippet>>

    @Query("SELECT * FROM saved_code_snippet WHERE language = :language ORDER BY timestamp DESC")
    fun getSnippetsByLanguage(language: String): Flow<List<SavedCodeSnippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SavedCodeSnippet)

    @Delete
    suspend fun deleteSnippet(snippet: SavedCodeSnippet)

    @Query("DELETE FROM saved_code_snippet WHERE id = :id")
    suspend fun deleteSnippetById(id: Int)
}
