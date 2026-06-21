package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.LanguageType
import com.example.ui.components.DashboardScreen
import com.example.ui.components.PlaygroundScreen
import com.example.ui.components.SandboxCabinetScreen
import com.example.ui.components.TutorChatScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TutorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Mandatory Edge-to-Edge handling
        setContent {
            MyApplicationTheme {
                MainContainerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen() {
    val viewModel: TutorViewModel = viewModel()

    // Observe DB states
    val completedLessons by viewModel.completedLessons.collectAsState()
    val highScores by viewModel.allHighScores.collectAsState()
    val savedSnippets by viewModel.savedSnippets.collectAsState()

    // Observe active tabs & selection layouts
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val activeLesson by viewModel.currentLesson.collectAsState()
    val activeQuizLang by viewModel.activeQuizLanguage.collectAsState()

    // Determine if we should show standard top bar/bottom bar
    val showScaffoldDecoration = activeQuizLang == null && activeLesson == null && selectedLanguage == null

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars), // Notch clip prevention Top
        topBar = {
            if (showScaffoldDecoration) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> "CodeMaster"
                                1 -> "Sandbox Console"
                                2 -> "AI Tutor Codey"
                                else -> "My Library"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (showScaffoldDecoration) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), // gesture nav bar padding bottom
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Learn") },
                        label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Playground") },
                        label = { Text("Playground", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Tutor") },
                        label = { Text("Ask Codey", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Sandbox Cabinet") },
                        label = { Text("Saved", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing // standard window insets constraints
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        completedLessons = completedLessons,
                        highScores = highScores,
                        savedSnippets = savedSnippets
                    )
                }
                1 -> {
                    PlaygroundScreen(viewModel = viewModel)
                }
                2 -> {
                    TutorChatScreen(viewModel = viewModel)
                }
                3 -> {
                    SandboxCabinetScreen(viewModel = viewModel, savedSnippets = savedSnippets)
                }
            }
        }
    }
}
