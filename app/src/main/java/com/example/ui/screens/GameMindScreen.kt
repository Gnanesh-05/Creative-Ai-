package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.GamesViewModel

data class GameCardItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val accentColor: Color
)

@Composable
fun GameMindScreen(
    gamesViewModel: GamesViewModel,
    onNavigateToGame: (String) -> Unit
) {
    val uiState by gamesViewModel.uiState.collectAsState()
    val tabs = listOf("Play Games", "History", "Stats", "Profile & Settings")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .padding(16.dp)
            .testTag("game_mind_screen_container")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFB4A2).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFFFFB4A2), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Game Mind AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Interactive Gaming & Strategic Engine Hub", fontSize = 11.sp, color = Color(0xFFFFB4A2))
                }
            }

            IconButton(
                onClick = { gamesViewModel.askAiCoach("General Strategy") },
                modifier = Modifier.testTag("game_mind_ai_coach_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach", tint = Color(0xFF4FD8EB))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs
        TabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = Color(0xFF1E1938),
            contentColor = Color(0xFFD0BCFF),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { gamesViewModel.selectTab(index) },
                    text = {
                        Text(
                            title,
                            fontSize = 12.sp,
                            fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == index) Color(0xFF4FD8EB) else Color.White.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.testTag("game_mind_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.selectedTab) {
            0 -> PlayGamesTab(onNavigateToGame)
            1 -> GameHistoryTab(uiState.gameHistory)
            2 -> GameStatsTab(uiState)
            3 -> GameProfileSettingsTab(gamesViewModel, uiState)
        }

        // AI Coach Advice Dialog
        if (uiState.aiCoachDialogTitle != null) {
            AlertDialog(
                onDismissRequest = { gamesViewModel.dismissAiCoachDialog() },
                containerColor = Color(0xFF1E1938),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF4FD8EB))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(uiState.aiCoachDialogTitle ?: "AI Coach Advice", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    if (uiState.isAiCoachLoading) {
                        CircularProgressIndicator(color = Color(0xFF4FD8EB), modifier = Modifier.padding(16.dp))
                    } else {
                        Text(uiState.aiCoachMessage ?: "", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { gamesViewModel.dismissAiCoachDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD8EB))
                    ) {
                        Text("Got it", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun PlayGamesTab(onNavigateToGame: (String) -> Unit) {
    val games = listOf(
        GameCardItem("Chess vs AI", "Grandmaster engine move evaluation & positional logic", Icons.Default.SportsEsports, "chess", Color(0xFFFFB4A2)),
        GameCardItem("Tic-Tac-Toe vs AI", "Unbeatable Minimax AI engine with 3 difficulty levels", Icons.Default.GridOn, "tictactoe", Color(0xFFD0BCFF)),
        GameCardItem("AI Maze Pathfinder", "Procedural maze generation & A* pathfinding solver", Icons.Default.Extension, "maze", Color(0xFF4FD8EB))
    )

    LazyColumn {
        items(games) { game ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onNavigateToGame(game.route) }
                    .testTag("game_mind_item_${game.route}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(game.accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(game.icon, contentDescription = game.title, tint = game.accentColor, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(game.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(game.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
fun GameHistoryTab(history: List<com.example.ui.viewmodels.GameHistoryRecord>) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No game records yet. Play a game to see your history!", color = Color.White.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history) { record ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(record.gameType, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${record.totalMoves} moves • Score: ${record.score}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (record.result) {
                                "Won" -> Color(0xFF4FD8EB).copy(alpha = 0.2f)
                                "Draw" -> Color(0xFFD0BCFF).copy(alpha = 0.2f)
                                else -> Color(0xFFFFB4A2).copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = record.result,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (record.result) {
                                    "Won" -> Color(0xFF4FD8EB)
                                    "Draw" -> Color(0xFFD0BCFF)
                                    else -> Color(0xFFFFB4A2)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameStatsTab(uiState: com.example.ui.viewmodels.GamesUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatSummaryCard("Tic-Tac-Toe vs Minimax", uiState.tictactoeWins, uiState.tictactoeLosses, uiState.tictactoeDraws, Color(0xFFD0BCFF))
        StatSummaryCard("Chess vs AI Engine", 1, 0, 0, Color(0xFFFFB4A2))
        StatSummaryCard("AI Maze Pathfinder", 3, 0, 0, Color(0xFF4FD8EB))
    }
}

@Composable
fun StatSummaryCard(title: String, wins: Int, losses: Int, draws: Int, accentColor: Color) {
    val total = wins + losses + draws
    val winRate = if (total > 0) (wins.toFloat() / total * 100).toInt() else 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Win Rate: $winRate%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wins", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("$wins", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Losses", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("$losses", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB4A2))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Draws", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("$draws", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                }
            }
        }
    }
}

@Composable
fun GameProfileSettingsTab(gamesViewModel: GamesViewModel, uiState: com.example.ui.viewmodels.GamesUiState) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Player Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Tic-Tac-Toe Difficulty", fontSize = 13.sp, color = Color(0xFFD0BCFF))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    listOf("Easy", "Medium", "Unbeatable").forEach { diff ->
                        FilterChip(
                            selected = uiState.ticTacToeDifficulty == diff,
                            onClick = { gamesViewModel.setTicTacToeDifficulty(diff) },
                            label = { Text(diff, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Chess Difficulty", fontSize = 13.sp, color = Color(0xFFFFB4A2))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        FilterChip(
                            selected = uiState.chessDifficulty == diff,
                            onClick = { gamesViewModel.setChessDifficulty(diff) },
                            label = { Text(diff, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}
