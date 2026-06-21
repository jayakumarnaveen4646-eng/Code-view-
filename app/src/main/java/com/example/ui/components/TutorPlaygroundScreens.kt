package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageType
import com.example.data.model.SavedCodeSnippet
import com.example.ui.viewmodel.TutorViewModel
import com.example.util.CodeExecutor

// --- 1. SCRIPT CONSOLE WORKSPACE SCREEN (PLAYGROUND) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaygroundScreen(viewModel: TutorViewModel) {
    val playLang by viewModel.playgroundLanguage.collectAsState()
    val playCode by viewModel.playgroundCode.collectAsState()
    val execResult by viewModel.executionResult.collectAsState()
    val isExec by viewModel.isExecuting.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var inputSnippetTitle by remember { mutableStateOf("") }
    
    var activeOutputPaneTab by remember { mutableStateOf(0) } // 0: Terminal Log, 1: HTML Render Frame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dropdown Language Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sandbox Console",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LanguageType.values().forEach { lang ->
                    FilterChip(
                        selected = playLang == lang,
                        onClick = { viewModel.updatePlaygroundLanguage(lang) },
                        label = { Text(lang.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = lang.primaryColor.copy(alpha = 0.2f),
                            selectedLabelColor = lang.primaryColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = playLang == lang,
                            borderColor = Color.Transparent,
                            selectedBorderColor = lang.primaryColor
                        )
                    )
                }
            }
        }

        // Editor Screen Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 320.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, playLang.primaryColor.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Editor header metadata
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121820))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "main.${getLangSuffix(playLang)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = playLang.primaryColor
                    )
                    IconButton(
                        onClick = { viewModel.updatePlaygroundCode("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Editor", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }

                // Editor Area with Custom Row-Numbers Left-Align
                Row(modifier = Modifier.fillMaxSize()) {
                    // Raw numbers column
                    Column(
                        modifier = Modifier
                            .background(Color(0xFF181818))
                            .padding(vertical = 12.dp, horizontal = 6.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.End
                    ) {
                        val linesCount = playCode.split("\n").size.coerceAtLeast(1)
                        for (idx in 1..linesCount) {
                            Text(
                                text = "$idx",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color.Gray.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // Pure dynamic code input text area
                    BasicTextField(
                        value = playCode,
                        onValueChange = { viewModel.updatePlaygroundCode(it) },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFE4E4E4)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(12.dp),
                        cursorBrush = SolidColor(playLang.primaryColor)
                    )
                }
            }
        }

        // Code Sandbox Quick actions Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // EXECUTE RUN LOGS
            Button(
                onClick = {
                    viewModel.runPlaygroundCode()
                    if (playLang == LanguageType.HTML) {
                        activeOutputPaneTab = 1 // force to render tab for html
                    } else {
                        activeOutputPaneTab = 0 // force to terminal tab
                    }
                },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = playLang.primaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isExec) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Code", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // AI EXPLAIN WITH CODEY
            Button(
                onClick = { viewModel.explainCodeWithAI() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "Ask Tutor", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI Explain", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // SAVE CLIP DIALOG
            OutlinedButton(
                onClick = {
                    inputSnippetTitle = "My ${playLang.displayName} File"
                    showSaveDialog = true
                },
                modifier = Modifier.weight(0.8f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = playLang.primaryColor),
                border = BorderStroke(1.dp, playLang.primaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Snippet", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // OUTPUT CONSOLE PANE
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Pane tabs selector
                TabRow(
                    selectedTabIndex = activeOutputPaneTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
                ) {
                    Tab(
                        selected = activeOutputPaneTab == 0,
                        onClick = { activeOutputPaneTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = "Terminal", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Terminal Console", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeOutputPaneTab == 1,
                        onClick = { activeOutputPaneTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Html, contentDescription = "Render", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("HTML Render Canvas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Pane output visualizers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .background(
                            if (activeOutputPaneTab == 0) Color(0xFF1E1E1E) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(if (activeOutputPaneTab == 0) 12.dp else 0.dp)
                ) {
                    if (activeOutputPaneTab == 0) {
                        // TERMINAL CONSOLE LOGS
                        val res = execResult
                        if (res != null) {
                            if (res.success) {
                                Text(
                                    text = res.output,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE4E4E4)
                                )
                            } else {
                                Text(
                                    text = res.errors,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF5252)
                                )
                            }
                        } else {
                            Text(
                                text = "Compiler output idle. Click Run to print results of your code...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // HTML CANVAS VISUAL RENDER
                        if (playLang == LanguageType.HTML) {
                            HTMLRenderView(htmlCode = playCode)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "HTML Render Frame is active only for HTML layouts. Change Sandbox Language above!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to save snippets
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Code Snippet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Give a title to save this ${playLang.displayName} playground code into your personal sandbox library:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = inputSnippetTitle,
                        onValueChange = { inputSnippetTitle = it },
                        placeholder = { Text("e.g., My Functions File") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputSnippetTitle.trim().isNotEmpty()) {
                            viewModel.saveSandboxSnippet(inputSnippetTitle)
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = playLang.primaryColor)
                ) {
                    Text("Save Snippet", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getLangSuffix(lang: LanguageType): String {
    return when (lang) {
        LanguageType.PYTHON -> "py"
        LanguageType.JAVA -> "java"
        LanguageType.JAVASCRIPT -> "js"
        LanguageType.HTML -> "html"
    }
}

// --- 2. PERSONAL AI TUTOR CODEY CHAT SCREEN ---

@Composable
fun TutorChatScreen(viewModel: TutorViewModel) {
    val history by viewModel.chatHistory.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()

    val chatListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto scroll chat to bottom when bubbles update
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            chatListState.animateScrollToItem(history.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Chat Header Status Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (isAILoading) Color(0xFFF7DF1E) else Color(0xFF4CAF50), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAILoading) "Codey is coding..." else "Codey is Online",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = { viewModel.clearChatHistory() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ClearAll, contentDescription = "Clear Chat", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear logs", fontSize = 12.sp)
            }
        }

        // Bubbles column list
        LazyColumn(
            state = chatListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(history) { chatPair ->
                ChatBubbleRow(messageMsg = chatPair.first, isUser = chatPair.second)
            }

            if (isAILoading) {
                item {
                    ChatThinkingBubble()
                }
            }
        }

        // Bottom dialog input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.updateChatInput(it) },
                placeholder = { Text("Ask Codey: 'How do loops work in Java?'") },
                modifier = Modifier
                    .weight(1f),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.sendChatMessage()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            FloatingActionButton(
                onClick = {
                    viewModel.sendChatMessage()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ChatBubbleRow(messageMsg: String, isUser: Boolean) {
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerBg = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val roundShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(roundShape)
                .background(containerBg)
                .padding(12.dp)
        ) {
            Column {
                if (!isUser) {
                    Text(
                        "Codey AI Tutor",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                // If message contains formatted markdown blocks, parse code blocks easily
                if (messageMsg.contains("```")) {
                    FormattedMarkdownBody(messageStr = messageMsg, defaultColor = contentColor)
                } else {
                    Text(
                        text = messageMsg,
                        fontSize = 14.sp,
                        color = contentColor,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownBody(messageStr: String, defaultColor: Color) {
    // split by code block tokens: ```
    val markdownComps = remember(messageStr) { messageStr.split("```") }
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        markdownComps.forEachIndexed { index, term ->
            if (index % 2 != 0) {
                // Odd block represents Code block!
                val cleanCode = term.trim()
                // extract optional language header if first row has word
                val firstNewLine = cleanCode.indexOf('\n')
                var finalCode = cleanCode
                var codeLangHeader = "CODE"
                if (firstNewLine != -1) {
                    val possibleHeader = cleanCode.substring(0, firstNewLine).trim()
                    if (possibleHeader.length < 15 && possibleHeader.isNotEmpty()) {
                        codeLangHeader = possibleHeader.uppercase()
                        finalCode = cleanCode.substring(firstNewLine + 1).trim()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF15181E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = codeLangHeader,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Text(
                            text = finalCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFDDDDF0),
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                // Even block represents plain markdown desc
                val cleanDesc = term.trim()
                if (cleanDesc.isNotEmpty()) {
                    Text(
                        text = cleanDesc,
                        fontSize = 14.sp,
                        color = defaultColor,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 150.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Codey is typing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Small blinking text indicators
                Text(".", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(".", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(".", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- 3. PERSONAL SANDBOX CODE COLLECTION CABINET ---

@Composable
fun SandboxCabinetScreen(viewModel: TutorViewModel, savedSnippets: List<SavedCodeSnippet>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "My Personal Code Library",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "Manage your custom code snippets and execute/edit them anytime inside the Sandbox Workspace below.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (savedSnippets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your library cabinet is empty.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Try writing custom code inside the Sandbox Console and click 'SaveSnippet' to collect them here!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(savedSnippets) { snippet ->
                    SavedSnippetListItem(
                        snippet = snippet,
                        onLoad = { viewModel.loadSavedSnippetIntoPlayground(snippet) },
                        onDelete = { viewModel.deleteSnippet(snippet) }
                    )
                }
            }
        }
    }
}
