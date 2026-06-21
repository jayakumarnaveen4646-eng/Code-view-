package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiTutorService
import com.example.data.db.AppDatabase
import com.example.data.model.LanguageType
import com.example.data.model.Lesson
import com.example.data.model.LessonCompletion
import com.example.data.model.QuizHighScore
import com.example.data.model.QuizQuestion
import com.example.data.model.SavedCodeSnippet
import com.example.data.provider.StaticDataProvider
import com.example.data.repository.TutorRepository
import com.example.util.CodeExecutor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TutorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TutorRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TutorRepository(
            db.lessonCompletionDao(),
            db.quizHighScoreDao(),
            db.savedCodeSnippetDao()
        )
    }

    // --- State Observables mapped from Room ---
    val savedSnippets: StateFlow<List<SavedCodeSnippet>> = repository.getAllSnippets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHighScores: StateFlow<List<QuizHighScore>> = repository.getAllHighScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Language-specific observables updated on-demand
    private val _completedLessons = MutableStateFlow<List<LessonCompletion>>(emptyList())
    val completedLessons: StateFlow<List<LessonCompletion>> = _completedLessons.asStateFlow()

    private val _isCleared = MutableSharedFlow<Unit>()

    // --- Navigation & Interactive Screen States ---
    private val _selectedTab = MutableStateFlow(0) // 0: Learn/Dashboard, 1: Playground, 2: AI Tutor, 3: Sandbox
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<LanguageType?>(null)
    val selectedLanguage: StateFlow<LanguageType?> = _selectedLanguage.asStateFlow()

    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()

    // --- Quiz States ---
    private val _activeQuizLanguage = MutableStateFlow<LanguageType?>(null)
    val activeQuizLanguage: StateFlow<LanguageType?> = _activeQuizLanguage.asStateFlow()

    private val _currentQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val currentQuizQuestions: StateFlow<List<QuizQuestion>> = _currentQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _quizSelectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // map of QuestionIndex -> SelectedOptionIndex
    val quizSelectedAnswers: StateFlow<Map<Int, Int>> = _quizSelectedAnswers.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    // --- Code Playground States ---
    private val _playgroundLanguage = MutableStateFlow(LanguageType.PYTHON)
    val playgroundLanguage: StateFlow<LanguageType> = _playgroundLanguage.asStateFlow()

    private val _playgroundCode = MutableStateFlow("")
    val playgroundCode: StateFlow<String> = _playgroundCode.asStateFlow()

    private val _executionResult = MutableStateFlow<CodeExecutor.ExecutionResult?>(null)
    val executionResult: StateFlow<CodeExecutor.ExecutionResult?> = _executionResult.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    // --- AI Chat States --
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList()) // text to isUser
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    // --- Initial Configurations ---
    init {
        // Set up default playground template
        updatePlaygroundLanguage(LanguageType.PYTHON)
        loadChatHistoryPlaceholder()
    }

    private fun loadChatHistoryPlaceholder() {
        _chatHistory.value = listOf(
            "Hello! I am Codey, your personalized AI coding tutor. Ask me any question about Python, Java, JavaScript, or HTML, and I’ll help you master it step-by-step!" to false
        )
    }

    // --- Action Handlers ---
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        if (tabIndex == 1 && _playgroundCode.value.isEmpty()) {
            // Lazy load selected language template if blank
            loadTemplateForPlayground()
        }
    }

    fun selectLanguage(language: LanguageType?) {
        _selectedLanguage.value = language
        _currentLesson.value = null
        if (language != null) {
            observeCompletedLessons(language.id)
        }
    }

    private fun observeCompletedLessons(languageId: String) {
        viewModelScope.launch {
            repository.getCompletedLessons(languageId).collect {
                _completedLessons.value = it
            }
        }
    }

    fun selectLesson(lesson: Lesson?) {
        _currentLesson.value = lesson
        if (lesson != null) {
            _playgroundLanguage.value = LanguageType.fromId(lesson.language)
            _playgroundCode.value = lesson.codeSnippet
            _executionResult.value = null
        }
    }

    fun tryLessonInPlayground(lesson: Lesson) {
        _playgroundLanguage.value = LanguageType.fromId(lesson.language)
        _playgroundCode.value = lesson.codeSnippet
        _executionResult.value = null
        _selectedTab.value = 1 // Switch to Playground Tab
    }

    fun completeCurrentLesson() {
        val lesson = _currentLesson.value ?: return
        viewModelScope.launch {
            repository.saveLessonCompletion(lesson.language, lesson.id, true)
            // Re-fetch completion lists
            observeCompletedLessons(lesson.language)
            
            // Advance to next lesson if available
            val currentList = StaticDataProvider.lessonsMap[LanguageType.fromId(lesson.language)] ?: emptyList()
            val nextIdx = currentList.indexOfFirst { it.id == lesson.id } + 1
            if (nextIdx in currentList.indices) {
                _currentLesson.value = currentList[nextIdx]
            } else {
                _currentLesson.value = null // Finished course!
            }
        }
    }

    fun resetCompletedLessons(language: String) {
        viewModelScope.launch {
            repository.clearCompletedLessons(language)
            observeCompletedLessons(language)
        }
    }

    // --- Quiz Actions ---
    fun startQuiz(language: LanguageType) {
        _activeQuizLanguage.value = language
        _currentQuizQuestions.value = StaticDataProvider.quizzesMap[language] ?: emptyList()
        _currentQuestionIndex.value = 0
        _quizSelectedAnswers.value = emptyMap()
        _isQuizFinished.value = false
        _quizScore.value = 0
    }

    fun answerQuizQuestion(questionIndex: Int, optionIndex: Int) {
        val updated = _quizSelectedAnswers.value.toMutableMap()
        updated[questionIndex] = optionIndex
        _quizSelectedAnswers.value = updated
    }

    fun nextQuizQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _currentQuizQuestions.value.size) {
            _currentQuestionIndex.value = nextIdx
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        val questions = _currentQuizQuestions.value
        val answers = _quizSelectedAnswers.value
        var score = 0
        questions.forEachIndexed { idx, question ->
            if (answers[idx] == question.correctAnswerIndex) {
                score++
            }
        }
        _quizScore.value = score
        _isQuizFinished.value = true

        val lang = _activeQuizLanguage.value ?: return
        viewModelScope.launch {
            // Fetch current high score to compare
            repository.getHighScore(lang.id).firstOrNull()?.let { currentHighScore ->
                if (score > currentHighScore.highScore) {
                    repository.saveHighScore(lang.id, score, questions.size)
                }
            } ?: run {
                repository.saveHighScore(lang.id, score, questions.size)
            }
        }
    }

    fun exitQuiz() {
        _activeQuizLanguage.value = null
        _currentQuizQuestions.value = emptyList()
        _isQuizFinished.value = false
    }

    // --- Playground & Sandbox Actions ---
    fun updatePlaygroundLanguage(language: LanguageType) {
        _playgroundLanguage.value = language
        loadTemplateForPlayground()
        _executionResult.value = null
    }

    fun updatePlaygroundCode(code: String) {
        _playgroundCode.value = code
    }

    private fun loadTemplateForPlayground() {
        val lang = _playgroundLanguage.value
        val template = when (lang) {
            LanguageType.PYTHON -> "# Write Python Sandbox scripts here\ndef multiply(val_a, val_b):\n    return val_a * val_b\n\nfactor = 6\nprint(\"Factor multiplier:\", multiply(factor, 8))"
            LanguageType.JAVA -> "public class SandboxMain {\n    public static void main(String[] args) {\n        System.out.println(\"Hello CodeMaster Java console!\");\n        int sum = 0;\n        for (int i = 1; i <= 5; i++) {\n            sum += i;\n        }\n        System.out.println(\"Total loop sum: \" + sum);\n    }\n}"
            LanguageType.JAVASCRIPT -> "// Interactive JavaScript playground\nconst profileUser = \"Naveen\";\nconst scoreRank = 95;\n\nconsole.log(\"User log: \" + profileUser);\nif (scoreRank > 90) {\n    console.log(\"Status: Elite coder unlocked!\");\n}"
            LanguageType.HTML -> "<!-- Interactive HTML output pre-view -->\n<div style=\"background-color: #2e3440; color: white; padding: 20px; border-radius: 12px; border: 2px solid #88c0d0;\">\n  <h1 style=\"margin: 0; color: #88c0d0;\">CodeMaster HTML Preview Frame</h1>\n  <p style=\"font-size: 15px; margin-top: 6px;\">Customize this template! Visual changes render instantly below.</p>\n  <button style=\"background-color: #81a1c1; color: #2e3440; font-weight: bold; border: none; padding: 10px 18px; border-radius: 6px; margin-top: 10px;\">Click action</button>\n</div>"
        }
        _playgroundCode.value = template
    }

    fun runPlaygroundCode() {
        _isExecuting.value = true
        viewModelScope.launch {
            val result = CodeExecutor.execute(_playgroundCode.value, _playgroundLanguage.value)
            _executionResult.value = result
            _isExecuting.value = false
        }
    }

    fun explainCodeWithAI() {
        val code = _playgroundCode.value
        val langName = _playgroundLanguage.value.displayName
        if (code.trim().isEmpty()) return

        _isAILoading.value = true
        _selectedTab.value = 2 // Switch to Chat Tab automatically
        
        val systemPrompt = "You are Codey, an expert code explanation engine. Analyze the provided $langName code snippet and explain it simply line-by-line using concise, neat paragraphs and clear markdown styling."
        val userPrompt = "Please analyze and explain this $langName code snippet step-by-step:\n\n```$langName\n$code\n```"

        // Insert prompt turn in visible chat history first
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add("Explain this $langName playground code block..." to true)
        _chatHistory.value = currentHistory

        viewModelScope.launch {
            val explantionStr = GeminiTutorService.getTutorResponse(
                prompt = userPrompt,
                systemInstruction = systemPrompt
            )
            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(explantionStr to false)
            _chatHistory.value = updatedHistory
            _isAILoading.value = false
        }
    }

    fun saveSandboxSnippet(title: String) {
        val code = _playgroundCode.value
        val lang = _playgroundLanguage.value.id
        if (code.trim().isEmpty() || title.trim().isEmpty()) return

        viewModelScope.launch {
            repository.saveSnippet(title.trim(), lang, code)
        }
    }

    fun deleteSnippet(snippet: SavedCodeSnippet) {
        viewModelScope.launch {
            repository.deleteSnippet(snippet)
        }
    }

    fun loadSavedSnippetIntoPlayground(snippet: SavedCodeSnippet) {
        val lang = LanguageType.fromId(snippet.language)
        _playgroundLanguage.value = lang
        _playgroundCode.value = snippet.code
        _executionResult.value = null
        _selectedTab.value = 1 // Switch to Playground
    }

    // --- AI Chat Actions ---
    fun updateChatInput(input: String) {
        _chatInput.value = input
    }

    fun sendChatMessage() {
        val input = _chatInput.value.trim()
        if (input.isEmpty()) return

        _chatInput.value = ""
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add(input to true)
        _chatHistory.value = currentHistory

        _isAILoading.value = true

        viewModelScope.launch {
            // Keep actual chat interaction within context (send last 5 turns to preserve token count)
            val historySubset = if (currentHistory.size > 10) {
                currentHistory.takeLast(10).dropLast(1)
            } else {
                currentHistory.dropLast(1)
            }

            val systemInstr = "You are Codey, an encouraging, friendly, and expert coding tutor for Python, Java, JavaScript, and HTML. Answer queries with simple examples, explain complexity simply, and guide users to correct syntax rather than just writing the code. Limit answers to three concise paragraphs or clear bullet points."

            val aiAnswer = GeminiTutorService.getTutorResponse(
                prompt = input,
                systemInstruction = systemInstr,
                history = historySubset
            )

            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(aiAnswer to false)
            _chatHistory.value = updatedHistory
            _isAILoading.value = false
        }
    }

    fun clearChatHistory() {
        _chatHistory.value = emptyList()
        loadChatHistoryPlaceholder()
    }
}
