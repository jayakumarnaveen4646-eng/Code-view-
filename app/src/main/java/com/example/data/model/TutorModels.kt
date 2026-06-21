package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class LanguageType(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val description: String,
    val iconName: String
) {
    PYTHON(
        "Python",
        "Python",
        Color(0xFF3776AB),
        "Dynamic, easy-to-read language perfect for beginners, data science, and AI development.",
        "snake"
    ),
    JAVA(
        "Java",
        "Java",
        Color(0xFF007396),
        "Robust, object-oriented language fueling Android applications and enterprise systems.",
        "coffee"
    ),
    JAVASCRIPT(
        "JavaScript",
        "JavaScript",
        Color(0xFFF7DF1E),
        "The language of the web. Essential for creating interactive and dynamic user interfaces.",
        "html5"
    ),
    HTML(
        "HTML",
        "HTML / CSS",
        Color(0xFFE34F26),
        "HyperText Markup Language. The structural skeleton of every single web page in existence.",
        "code"
    );

    companion object {
        fun fromId(id: String): LanguageType =
            values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PYTHON
    }
}

data class Lesson(
    val id: Int,
    val language: String,
    val title: String,
    val subtitle: String,
    val conceptExplain: String,
    val codeSnippet: String,
    val expectedOutput: String
)

data class QuizQuestion(
    val id: Int,
    val language: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)
