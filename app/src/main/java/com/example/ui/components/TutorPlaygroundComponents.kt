package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageType
import com.example.data.model.Lesson
import com.example.data.model.QuizQuestion
import com.example.data.model.SavedCodeSnippet
import com.example.data.provider.StaticDataProvider
import com.example.ui.viewmodel.TutorViewModel
import com.example.util.CodeExecutor

// --- MAIN COMPONENT VIEWS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TutorViewModel,
    completedLessons: List<com.example.data.model.LessonCompletion>,
    highScores: List<com.example.data.model.QuizHighScore>,
    savedSnippets: List<SavedCodeSnippet>
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val activeLesson by viewModel.currentLesson.collectAsState()
    val activeQuizLang by viewModel.activeQuizLanguage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (activeQuizLang != null) {
            QuizView(viewModel = viewModel)
        } else if (activeLesson != null) {
            LessonDetailView(viewModel = viewModel, lesson = activeLesson!!)
        } else if (selectedLanguage != null) {
            CourseOutlineView(
                viewModel = viewModel,
                language = selectedLanguage!!,
                completedLessons = completedLessons,
                highScore = highScores.firstOrNull { it.language.equals(selectedLanguage!!.id, ignoreCase = true) }
            )
        } else {
            // MAIN GRID SELECTOR
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                // Friendly Greeting Hero Banner
                item {
                    GreetingHeroBanner(
                        completedCount = completedLessons.size,
                        avgQuizScore = if (highScores.isEmpty()) 0 else (highScores.sumOf { it.highScore } * 100 / (highScores.sumOf { it.totalQuestions }.coerceAtLeast(1)))
                    )
                }

                item {
                    Text(
                        text = "Choose Your Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Grid of 4 languages
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LanguageType.values().toList().chunked(2).forEach { rowLangs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowLangs.forEach { language ->
                                    val countCompleted = completedLessons.count { it.language.equals(language.id, ignoreCase = true) }
                                    val high = highScores.firstOrNull { it.language.equals(language.id, ignoreCase = true) }

                                    LanguageSelectorCard(
                                        language = language,
                                        completedLessonsCount = countCompleted,
                                        totalLessons = 4, // static size per lang is 4
                                        quizHighScore = high?.highScore,
                                        onClick = { viewModel.selectLanguage(language) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Saved Snippets quick access row
                if (savedSnippets.isNotEmpty()) {
                    item {
                        Text(
                            text = "My Code Library",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(savedSnippets.take(3)) { snippet ->
                        SavedSnippetListItem(
                            snippet = snippet,
                            onLoad = { viewModel.loadSavedSnippetIntoPlayground(snippet) },
                            onDelete = { viewModel.deleteSnippet(snippet) }
                        )
                    }
                    if (savedSnippets.size > 3) {
                        item {
                            TextButton(
                                onClick = { viewModel.selectTab(3) }, // Switch to Saved tab
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View all library snippets (${savedSnippets.size})")
                                Icon(Icons.Default.ArrowForward, contentDescription = "View", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- BANNER VIEWS ---

@Composable
fun GreetingHeroBanner(completedCount: Int, avgQuizScore: Int) {
    val gradientBrushes = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrushes)
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Code",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hello Coding Coder!",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Ready to code today?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Lessons Done",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$completedCount completed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                VerticalDivider(
                    modifier = Modifier.height(30.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Avg Accuracy",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$avgQuizScore% correct",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LanguageSelectorCard(
    language: LanguageType,
    completedLessonsCount: Int,
    totalLessons: Int,
    quizHighScore: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(language.primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForName(language.iconName),
                        contentDescription = language.displayName,
                        tint = language.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = language.displayName,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = language.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }

            // progress tracker bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress: $completedLessonsCount/$totalLessons",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (quizHighScore != null) {
                        Text(
                            text = "Quiz: $quizHighScore/3",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { completedLessonsCount.toFloat() / totalLessons.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = language.primaryColor,
                    trackColor = language.primaryColor.copy(alpha = 0.1f)
                )
            }
        }
    }
}

// --- COURSE OUTLINE VIEW ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseOutlineView(
    viewModel: TutorViewModel,
    language: LanguageType,
    completedLessons: List<com.example.data.model.LessonCompletion>,
    highScore: com.example.data.model.QuizHighScore?
) {
    val lessons = StaticDataProvider.lessonsMap[language] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${language.displayName} Course", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectLanguage(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetCompletedLessons(language.id) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Course Progress")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Language overview banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = language.primaryColor.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(language.primaryColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForName(language.iconName),
                                contentDescription = language.displayName,
                                tint = language.primaryColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Mastering ${language.displayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Complete all 4 lessons to unlock the course challenge quiz!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Modules & Lessons",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Outline elements
            items(lessons) { lesson ->
                val isDone = completedLessons.any { it.lessonId == lesson.id && it.isCompleted }

                Card(
                    onClick = { viewModel.selectLesson(lesson) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDone) language.primaryColor.copy(alpha = 0.3f) else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isDone) language.primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = language.primaryColor, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = "${lesson.id}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = lesson.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lesson.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Lesson",
                            tint = language.primaryColor
                        )
                    }
                }
            }

            // Start Quiz Action Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.startQuiz(language) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = language.primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = "Quiz")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (highScore != null) "Retake Quiz (High Score: ${highScore.highScore}/3)" else "Take Course Challenge Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// --- LESSON CARD SLIDE VIEW ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailView(viewModel: TutorViewModel, lesson: Lesson) {
    val language = LanguageType.fromId(lesson.language)
    var localCommandResult by remember { mutableStateOf<CodeExecutor.ExecutionResult?>(null) }
    var isExecutingLocal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Lesson ${lesson.id}: ${lesson.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectLesson(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Body
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = lesson.subtitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = language.primaryColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = lesson.conceptExplain,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Syntax Sample Code Viewer
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Code Snippet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                CodeMirrorBlock(
                    codeStr = lesson.codeSnippet,
                    language = language
                )
            }

            // Quick Console Output inside the slide
            AnimatedVisibility(visible = localCommandResult != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Local Execution Console",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            if (localCommandResult?.success == true) {
                                Text(
                                    text = localCommandResult!!.output,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE8E8E8)
                                )
                            } else {
                                Text(
                                    text = localCommandResult?.errors ?: "Syntax Compile failure.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF5252)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            TextButton(
                                onClick = { localCommandResult = null },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Clear Console")
                            }
                        }
                    }
                }
            }

            // Interactive Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RUN EXECUTOR LOCALLY
                OutlinedButton(
                    onClick = {
                        isExecutingLocal = true
                        localCommandResult = CodeExecutor.execute(lesson.codeSnippet, language)
                        isExecutingLocal = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = language.primaryColor),
                    border = BorderStroke(1.dp, language.primaryColor)
                ) {
                    Icon(
                        imageVector = if (isExecutingLocal) Icons.Default.Circle else Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Log", fontWeight = FontWeight.Bold)
                }

                // TRY IN MAIN PLAYGROUND
                OutlinedButton(
                    onClick = { viewModel.tryLessonInPlayground(lesson) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = language.primaryColor),
                    border = BorderStroke(1.dp, language.primaryColor)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = "Playground", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Playground", fontWeight = FontWeight.Bold)
                }
            }

            // Got it! Complete Progress
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = { viewModel.completeCurrentLesson() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = language.primaryColor)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Complete")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mastered! Finish Module", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}

// --- SYNTAX HIGHLIGHT CODE CARD WRAPPER ---

@Composable
fun CodeMirrorBlock(codeStr: String, language: LanguageType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E1E))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.displayName.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = language.primaryColor
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFE34F26), CircleShape)
                )
            }

            // Core code text box with beautiful visual highlight representation
            Text(
                text = codeStr,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(0xFFE4E4E4),
                lineHeight = 18.sp
            )
        }
    }
}

// --- INTERACTIVE MULTIPLE CHOICE QUIZ WINDOW ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizView(viewModel: TutorViewModel) {
    val activeLang by viewModel.activeQuizLanguage.collectAsState()
    val questions by viewModel.currentQuizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswers by viewModel.quizSelectedAnswers.collectAsState()
    val isFinished by viewModel.isQuizFinished.collectAsState()
    val score by viewModel.quizScore.collectAsState()

    val currentQuestion = questions.getOrNull(currentIndex)
    val chosenOption = selectedAnswers[currentIndex]
    var showExplanation by remember(currentIndex) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${activeLang?.displayName} Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitQuiz() }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (isFinished) {
                // SCORE BLOCK
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(activeLang?.primaryColor?.copy(alpha = 0.15f) ?: Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = "Quiz Done",
                            tint = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Quiz Competed!",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Score: $score / ${questions.size}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (score) {
                            3 -> "Absolute Perfection! You are a certified ${activeLang?.displayName} CodeMaster! 🏆"
                            2 -> "Impressive effort! Just a few items left to learn. Keep practicing!"
                            else -> "Keep learning! Review the lesson outline modules and retake anytime."
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Button(
                    onClick = { viewModel.exitQuiz() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary)
                ) {
                    Text("Return to Course", fontWeight = FontWeight.Bold)
                }
            } else if (currentQuestion != null) {
                // THE QUESTION DISPLAY LAYOUT
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question index tracker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Score: ${selectedAnswers.filter { it.key < currentIndex }.count { questions[it.key].correctAnswerIndex == it.value }} / $currentIndex",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary
                    )

                    // Question Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = currentQuestion.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(18.dp)
                        )
                    }

                    // Answer Options selector
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        currentQuestion.options.forEachIndexed { optIdx, option ->
                            val isChosen = chosenOption == optIdx
                            val isCorrect = optIdx == currentQuestion.correctAnswerIndex
                            val optionBg = when {
                                chosenOption == null -> if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                isChosen && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                isChosen && !isCorrect -> Color(0xFFFF5252).copy(alpha = 0.15f)
                                !isChosen && isCorrect && chosenOption != null -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                            val optionBorderColor = when {
                                chosenOption == null -> if (isChosen) activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary else Color.Transparent
                                isChosen && isCorrect -> Color(0xFF4CAF50)
                                isChosen && !isCorrect -> Color(0xFFFF5252)
                                !isChosen && isCorrect && chosenOption != null -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                                else -> Color.Transparent
                            }

                            Card(
                                onClick = {
                                    if (chosenOption == null) {
                                        viewModel.answerQuizQuestion(currentIndex, optIdx)
                                        showExplanation = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = optionBg),
                                border = BorderStroke(
                                    1.2.dp,
                                    optionBorderColor
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                when {
                                                    chosenOption == null -> if (isChosen) activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                                    isChosen && isCorrect -> Color(0xFF4CAF50)
                                                    isChosen && !isCorrect -> Color(0xFFFF5252)
                                                    !isChosen && isCorrect && chosenOption != null -> Color(0xFF4CAF50)
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                                },
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (chosenOption != null && (isChosen && isCorrect || !isChosen && isCorrect)) {
                                            Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color.White, modifier = Modifier.size(14.dp))
                                        } else if (chosenOption != null && isChosen && !isCorrect) {
                                            Icon(Icons.Default.Close, contentDescription = "Wrong", tint = Color.White, modifier = Modifier.size(14.dp))
                                        } else {
                                            Text(
                                                text = when (optIdx) {
                                                    0 -> "A"
                                                    1 -> "B"
                                                    2 -> "C"
                                                    else -> "D"
                                                },
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp,
                                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Detailed explanation card if option is answered
                    AnimatedVisibility(visible = showExplanation && chosenOption != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Codey's Tip:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = currentQuestion.explanation,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Action validation footer button
                Button(
                    onClick = {
                        viewModel.nextQuizQuestion()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = chosenOption != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activeLang?.primaryColor ?: MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (currentIndex == questions.lastIndex) "Submit Quiz & Score" else "Continue",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- SAVED LIBRARY LIST ITEMS ---

@Composable
fun SavedSnippetListItem(
    snippet: SavedCodeSnippet,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val lang = LanguageType.fromId(snippet.language)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).clickable { onLoad() }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(lang.primaryColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForName(lang.iconName),
                        contentDescription = lang.displayName,
                        tint = lang.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = snippet.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Language: ${lang.displayName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onLoad) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Load",
                        tint = lang.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// --- UTILITY VECTOR DRAWABLE RESOLVER ---

private fun getIconForName(name: String): ImageVector {
    return when (name) {
        "snake" -> Icons.Default.BugReport
        "coffee" -> Icons.Default.Coffee
        "html5" -> Icons.Default.Html
        "code" -> Icons.Default.Code
        "quiz" -> Icons.Default.Quiz
        "tutor" -> Icons.Default.SmartToy
        else -> Icons.Default.Terminal
    }
}
