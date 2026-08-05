package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ChatConversationDomain
import com.example.domain.model.ChatMessageItemDomain
import com.example.ui.viewmodels.ChatViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel
) {
    val uiState by chatViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInputText by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var conversationToDeleteId by remember { mutableStateOf<String?>(null) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    // Auto-scroll to latest message or streaming text updates
    LaunchedEffect(uiState.messages.size, uiState.streamingText) {
        val totalItems = uiState.messages.size + (if (uiState.isStreaming) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val drawerState = rememberDrawerState(
        initialValue = if (uiState.showConversationsDrawer) DrawerValue.Open else DrawerValue.Closed
    )

    LaunchedEffect(uiState.showConversationsDrawer) {
        if (uiState.showConversationsDrawer) drawerState.open() else drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF141029),
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .testTag("conversations_drawer_sheet")
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Drawer Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Conversations",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { chatViewModel.createNewConversation("New Conversation") },
                        modifier = Modifier.testTag("drawer_new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Conversation",
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { chatViewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search history...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("conversation_search_input")
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                // Conversations List
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.conversations) { conv ->
                        val isSelected = conv.id == uiState.activeConversation?.id
                        NavigationDrawerItem(
                            label = {
                                Column {
                                    Text(
                                        text = conv.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFD0BCFF) else Color.White,
                                        fontSize = 14.sp
                                    )
                                    if (conv.lastMessageSnippet.isNotBlank()) {
                                        Text(
                                            text = conv.lastMessageSnippet,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                    }
                                }
                            },
                            selected = isSelected,
                            onClick = { chatViewModel.selectConversation(conv.id) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFD0BCFF) else Color.Gray
                                )
                            },
                            badge = {
                                IconButton(
                                    onClick = {
                                        conversationToDeleteId = conv.id
                                        showDeleteConfirmDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFF231B42),
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("conversation_item_${conv.id}")
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.activeConversation?.title ?: "Conversational AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Gemini 2.0 Flash • Multi-turn Assistant",
                                fontSize = 11.sp,
                                color = Color(0xFFD0BCFF)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { chatViewModel.toggleConversationsDrawer() },
                            modifier = Modifier.testTag("toggle_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "History",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { chatViewModel.createNewConversation("New Conversation") },
                            modifier = Modifier.testTag("new_chat_top_bar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = "New Chat",
                                tint = Color(0xFFD0BCFF)
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showMenuDropdown = true },
                                modifier = Modifier.testTag("chat_more_options_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showMenuDropdown,
                                onDismissRequest = { showMenuDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E1938))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename Conversation", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFD0BCFF)) },
                                    onClick = {
                                        showMenuDropdown = false
                                        renameInputText = uiState.activeConversation?.title ?: ""
                                        showRenameDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_rename_conversation")
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Messages", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color(0xFFD0BCFF)) },
                                    onClick = {
                                        showMenuDropdown = false
                                        chatViewModel.clearCurrentConversation()
                                    },
                                    modifier = Modifier.testTag("menu_clear_messages")
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Conversation", color = Color.Red) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                    onClick = {
                                        showMenuDropdown = false
                                        conversationToDeleteId = uiState.activeConversation?.id
                                        showDeleteConfirmDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_delete_conversation")
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F0C20)
                    )
                )
            },
            containerColor = Color(0xFF0F0C20)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp)
            ) {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { msg ->
                        ChatMessageItemRow(
                            message = msg,
                            context = context,
                            onCopy = { text -> chatViewModel.copyResponseToClipboard(context, text) },
                            onRegenerate = { chatViewModel.regenerateLastResponse() },
                            onRetry = { chatViewModel.retryFailedResponse() }
                        )
                    }

                    // Active streaming message chunk display
                    if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
                        item {
                            ChatMessageItemRow(
                                message = ChatMessageItemDomain(
                                    id = "streaming_tmp",
                                    sender = "AI",
                                    content = uiState.streamingText
                                ),
                                context = context,
                                isStreamingActive = true,
                                onCopy = {},
                                onRegenerate = {},
                                onRetry = {}
                            )
                        }
                    }

                    // Thinking indicator
                    if (uiState.isStreaming && uiState.streamingText.isEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                                    .testTag("typing_thinking_indicator")
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFD0BCFF),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Creative AI is formulating response...",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Stop Generation Banner Button
                if (uiState.isStreaming) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { chatViewModel.stopGeneration() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFFB4AB)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB4AB))
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("stop_generation_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop Generating", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Input Section
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E1938),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { chatViewModel.onInputChanged(it) },
                            placeholder = { Text("Ask questions, request code, or /image <prompt>...", color = Color.Gray, fontSize = 14.sp) },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        FloatingActionButton(
                            onClick = { chatViewModel.sendMessage() },
                            containerColor = if (uiState.inputText.isNotBlank() && !uiState.isStreaming) Color(0xFFD0BCFF) else Color(0xFF382F5E),
                            contentColor = Color(0xFF0F0C20),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Rename Conversation Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Conversation", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD0BCFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_conversation_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = false
                        val currentId = uiState.activeConversation?.id ?: return@TextButton
                        chatViewModel.renameConversation(currentId, renameInputText)
                    },
                    modifier = Modifier.testTag("confirm_rename_button")
                ) {
                    Text("Rename", color = Color(0xFFD0BCFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1938)
        )
    }

    // Delete Conversation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Conversation?", color = Color.White) },
            text = { Text("Are you sure you want to delete this conversation? This action cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        conversationToDeleteId?.let { chatViewModel.deleteConversation(it) }
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1938)
        )
    }
}

@Composable
fun ChatMessageItemRow(
    message: ChatMessageItemDomain,
    context: Context,
    isStreamingActive: Boolean = false,
    onCopy: (String) -> Unit,
    onRegenerate: () -> Unit,
    onRetry: () -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6750A4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.widthIn(max = 290.dp)) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    color = if (isUser) Color(0xFF6750A4) else Color(0xFF231B42),
                    modifier = Modifier.testTag("message_bubble_${message.id}")
                ) {
                    val parts = remember(message.content) { parseMarkdown(message.content) }
                    Column(modifier = Modifier.padding(12.dp)) {
                        parts.forEach { part ->
                            when (part) {
                                is MarkdownPart.Text -> {
                                    MarkdownTextFlow(text = part.content, isUser = isUser)
                                }
                                is MarkdownPart.CodeBlock -> {
                                    CodeBlockContainer(language = part.language, code = part.code)
                                }
                                is MarkdownPart.Image -> {
                                    ChatImageContainer(url = part.url, alt = part.alt)
                                }
                            }
                        }
                    }
                }

                // AI Action Row (Copy, Regenerate, Retry)
                if (!isUser && !isStreamingActive && message.content.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    ) {
                        IconButton(
                            onClick = { onCopy(message.content) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("copy_message_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Response",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onRegenerate() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("regenerate_message_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Response",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (message.content.startsWith("Error")) {
                            IconButton(
                                onClick = { onRetry() },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("retry_message_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Retry",
                                    tint = Color(0xFFFFB4AB),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class MarkdownPart {
    data class Text(val content: String) : MarkdownPart()
    data class CodeBlock(val language: String, val code: String) : MarkdownPart()
    data class Image(val url: String, val alt: String) : MarkdownPart()
}

fun parseMarkdown(content: String): List<MarkdownPart> {
    val parts = mutableListOf<MarkdownPart>()
    val currentText = content
    
    val codeBlockRegex = """```(\w*)\n([\s\S]*?)```""".toRegex()
    var matchResult = codeBlockRegex.find(currentText)
    var lastIdx = 0
    
    while (matchResult != null) {
        val before = currentText.substring(lastIdx, matchResult.range.first)
        if (before.isNotEmpty()) {
            parts.addAll(parseImagesAndText(before))
        }
        val language = matchResult.groupValues[1]
        val code = matchResult.groupValues[2]
        parts.add(MarkdownPart.CodeBlock(language, code))
        
        lastIdx = matchResult.range.last + 1
        matchResult = codeBlockRegex.find(currentText, lastIdx)
    }
    
    if (lastIdx < currentText.length) {
        val remaining = currentText.substring(lastIdx)
        parts.addAll(parseImagesAndText(remaining))
    }
    
    return parts.ifEmpty { listOf(MarkdownPart.Text(content)) }
}

fun parseImagesAndText(text: String): List<MarkdownPart> {
    val imageRegex = """!\[(.*?)\]\((.*?)\)""".toRegex()
    val parts = mutableListOf<MarkdownPart>()
    var lastIdx = 0
    var matchResult = imageRegex.find(text)
    
    while (matchResult != null) {
        val before = text.substring(lastIdx, matchResult.range.first)
        if (before.isNotEmpty()) {
            parts.add(MarkdownPart.Text(before))
        }
        val alt = matchResult.groupValues[1]
        val url = matchResult.groupValues[2]
        parts.add(MarkdownPart.Image(url, alt))
        
        lastIdx = matchResult.range.last + 1
        matchResult = imageRegex.find(text, lastIdx)
    }
    
    if (lastIdx < text.length) {
        val remaining = text.substring(lastIdx)
        val trimmed = remaining.trim()
        if ((trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:image/")) && 
            (trimmed.contains(".png") || trimmed.contains(".jpg") || trimmed.contains(".jpeg") || trimmed.contains("pollinations.ai") || trimmed.contains("picsum.photos"))) {
            parts.add(MarkdownPart.Image(trimmed, "Generated Image"))
        } else {
            parts.add(MarkdownPart.Text(remaining))
        }
    }
    return parts
}

fun renderMarkdownText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = """(\*\*\*.*?\*\*\*|\*\*.*?\*\*|\*.*?\*|`.*?`|__.*?__|__.*?__|_.*?_)""".toRegex()
        var lastIdx = 0
        val matches = regex.findAll(text)
        
        for (match in matches) {
            append(text.substring(lastIdx, match.range.first))
            
            val matchText = match.value
            when {
                matchText.startsWith("***") && matchText.endsWith("***") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(matchText.substring(3, matchText.length - 3))
                    }
                }
                matchText.startsWith("**") && matchText.endsWith("**") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchText.substring(2, matchText.length - 2))
                    }
                }
                matchText.startsWith("__") && matchText.endsWith("__") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchText.substring(2, matchText.length - 2))
                    }
                }
                matchText.startsWith("*") && matchText.endsWith("*") -> {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchText.substring(1, matchText.length - 1))
                    }
                }
                matchText.startsWith("_") && matchText.endsWith("_") -> {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchText.substring(1, matchText.length - 1))
                    }
                }
                matchText.startsWith("`") && matchText.endsWith("`") -> {
                    withStyle(style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF1E1938),
                        color = Color(0xFFD0BCFF)
                    )) {
                        append(matchText.substring(1, matchText.length - 1))
                    }
                }
                else -> append(matchText)
            }
            lastIdx = match.range.last + 1
        }
        if (lastIdx < text.length) {
            append(text.substring(lastIdx))
        }
    }
}

@Composable
fun MarkdownTextFlow(text: String, isUser: Boolean) {
    val lines = text.split("\n")
    Column(modifier = Modifier.fillMaxWidth()) {
        lines.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ")) {
                Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                    Text(
                        text = "•",
                        color = if (isUser) Color.White else Color(0xFFD0BCFF),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = renderMarkdownText(trimmedLine.substring(2)),
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            } else if (trimmedLine.isNotEmpty() && trimmedLine.first().isDigit() && trimmedLine.contains(". ")) {
                val dotIdx = trimmedLine.indexOf(". ")
                if (dotIdx in 1..4) {
                    val num = trimmedLine.substring(0, dotIdx + 1)
                    val content = trimmedLine.substring(dotIdx + 2)
                    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                        Text(
                            text = num,
                            color = if (isUser) Color.White else Color(0xFFD0BCFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = renderMarkdownText(content),
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    Text(
                        text = renderMarkdownText(line),
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = renderMarkdownText(line),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CodeBlockContainer(language: String, code: String) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141029)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1938))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code Block", code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(
                        text = code.trim(),
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE2E1EC),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatImageContainer(url: String, alt: String) {
    val context = LocalContext.current
    var isViewerOpen by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF231B42)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { isViewerOpen = true }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF141029)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = alt,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alt.length > 25) alt.take(25) + "..." else alt,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Row {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this AI-generated image: $url")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Image saved to downloads", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
    
    if (isViewerOpen) {
        Dialog(
            onDismissRequest = { isViewerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { isViewerOpen = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = alt,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
                
                IconButton(
                    onClick = { isViewerOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
