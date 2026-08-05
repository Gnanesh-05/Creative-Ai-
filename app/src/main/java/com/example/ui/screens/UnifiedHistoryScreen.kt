package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HistoryItemDomain
import com.example.ui.viewmodels.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedHistoryScreen(
    historyViewModel: HistoryViewModel,
    onOpenChat: (conversationId: String?) -> Unit = {},
    onOpenImage: (imageUrl: String?) -> Unit = {},
    onOpenMusic: (audioUrl: String?) -> Unit = {},
    onOpenGame: (gameType: String, gameData: String?) -> Unit = { _, _ -> }
) {
    val uiState by historyViewModel.uiState.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<HistoryItemDomain?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "ALL" to "All",
        "CHAT" to "Chat",
        "IMAGE" to "Images",
        "MUSIC" to "Music",
        "GAME_MIND" to "Game Mind",
        "GAME_CHESS" to "Chess",
        "GAME_TICTACTOE" to "Tic-Tac-Toe",
        "GAME_MAZE" to "Maze"
    )

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All History?") },
            text = { Text("Are you sure you want to permanently delete all your activity history? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        historyViewModel.clearAll()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false },
                    modifier = Modifier.testTag("cancel_clear_all_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("clear_all_dialog")
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Item") },
            text = { Text("Delete '${item.title}' from history?") },
            confirmButton = {
                Button(
                    onClick = {
                        historyViewModel.deleteItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_item_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("unified_history_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Unified History",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Personal activity & creative generation logs",
                            fontSize = 12.sp,
                            color = Color(0xFFCCC2DC)
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteAllDialog = true },
                    enabled = uiState.items.isNotEmpty(),
                    modifier = Modifier.testTag("history_clear_all_button")
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear All History",
                        tint = if (uiState.items.isNotEmpty()) Color(0xFFFFB4AB) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar & Sort Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { historyViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search logs...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFD0BCFF)) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { historyViewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFF49454F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier
                            .background(Color(0xFF2B2544), RoundedCornerShape(16.dp))
                            .size(52.dp)
                            .testTag("history_sort_button")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort Options", tint = Color(0xFFD0BCFF))
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF2B2544))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest First", color = Color.White) },
                            onClick = {
                                historyViewModel.setSortOrder("newest")
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (uiState.sortOrder == "newest") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFD0BCFF))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest First", color = Color.White) },
                            onClick = {
                                historyViewModel.setSortOrder("oldest")
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (uiState.sortOrder == "oldest") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFD0BCFF))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Title (A-Z)", color = Color.White) },
                            onClick = {
                                historyViewModel.setSortOrder("title")
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (uiState.sortOrder == "title") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFD0BCFF))
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips Row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.first == uiState.selectedCategory }.coerceAtLeast(0),
                containerColor = Color.Transparent,
                contentColor = Color(0xFFD0BCFF),
                edgePadding = 0.dp,
                modifier = Modifier.testTag("history_category_tabs")
            ) {
                categories.forEach { (catKey, catLabel) ->
                    Tab(
                        selected = uiState.selectedCategory == catKey,
                        onClick = { historyViewModel.setCategory(catKey) },
                        text = {
                            Text(
                                catLabel,
                                fontSize = 13.sp,
                                fontWeight = if (uiState.selectedCategory == catKey) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedCategory == catKey) Color(0xFFD0BCFF) else Color(0xFF8E88A1)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFD0BCFF))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Syncing history logs...", color = Color(0xFFCCC2DC), fontSize = 13.sp)
                    }
                }
            }
            // Error state
            else if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .testTag("history_error_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "Failed to load history",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { historyViewModel.fetchHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            // Empty state
            else if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("history_empty_state")
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No history found", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try selecting another category or resetting your search filter.",
                            color = Color(0xFF8E88A1),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            // Items List
            else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_items_list")
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            onOpen = {
                                when (item.moduleType.uppercase()) {
                                    "CHAT" -> onOpenChat(item.id)
                                    "IMAGE" -> onOpenImage(item.summary)
                                    "MUSIC" -> onOpenMusic(item.summary)
                                    else -> onOpenGame(item.moduleType, item.summary)
                                }
                            },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }

                // Pagination Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { historyViewModel.prevPage() },
                        enabled = uiState.currentPage > 1,
                        modifier = Modifier.testTag("history_prev_page_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                            Text("Previous")
                        }
                    }

                    Text(
                        "Page ${uiState.currentPage}",
                        color = Color(0xFFD0BCFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(
                        onClick = { historyViewModel.nextPage() },
                        enabled = uiState.hasMore,
                        modifier = Modifier.testTag("history_next_page_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next")
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: HistoryItemDomain,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, badgeColor) = getModuleIconAndColor(item.moduleType)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onOpen() }
            .testTag("history_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = getModuleLabel(item.moduleType),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.summary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.summary,
                        fontSize = 12.sp,
                        color = Color(0xFFCCC2DC),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete item", tint = Color(0xFFFFB4AB))
            }
        }
    }
}

private fun getModuleIconAndColor(moduleType: String): Pair<ImageVector, Color> {
    return when (moduleType.uppercase()) {
        "CHAT" -> Icons.Default.Chat to Color(0xFF81C784)
        "IMAGE" -> Icons.Default.Image to Color(0xFF64B5F6)
        "MUSIC" -> Icons.Default.MusicNote to Color(0xFFFFB74D)
        "GAME_MIND" -> Icons.Default.Psychology to Color(0xFFBA68C8)
        "GAME_CHESS" -> Icons.Default.Extension to Color(0xFFE57373)
        "GAME_TICTACTOE" -> Icons.Default.GridOn to Color(0xFF4DD0E1)
        "GAME_MAZE" -> Icons.Default.Explore to Color(0xFFFF8A65)
        else -> Icons.Default.Bookmark to Color(0xFFD0BCFF)
    }
}

private fun getModuleLabel(moduleType: String): String {
    return when (moduleType.uppercase()) {
        "CHAT" -> "Chat"
        "IMAGE" -> "Image"
        "MUSIC" -> "Music"
        "GAME_MIND" -> "Game Mind"
        "GAME_CHESS" -> "Chess"
        "GAME_TICTACTOE" -> "Tic-Tac-Toe"
        "GAME_MAZE" -> "Maze"
        else -> moduleType
    }
}
