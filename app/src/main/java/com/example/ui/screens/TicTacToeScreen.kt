package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.GamesViewModel

@Composable
fun TicTacToeScreen(
    gamesViewModel: GamesViewModel
) {
    val uiState by gamesViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tic-Tac-Toe vs Minimax AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = when (uiState.ticTacToeWinner) {
                        "X" -> if (uiState.playerSymbol == "X") "You Won! 🎉" else "AI Won!"
                        "O" -> if (uiState.playerSymbol == "O") "You Won! 🎉" else "AI Won!"
                        "DRAW" -> "Draw Game! 🤝"
                        else -> if (uiState.isTicTacToeLoading) "AI is calculating minimax..." else "Your turn (${uiState.playerSymbol})"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFFD0BCFF)
                )
            }
            Row {
                IconButton(onClick = { gamesViewModel.askAiCoach("Tic-Tac-Toe") }, modifier = Modifier.testTag("tictactoe_ai_coach_button")) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach", tint = Color(0xFF4FD8EB))
                }
                IconButton(onClick = { gamesViewModel.resetTicTacToe() }, modifier = Modifier.testTag("tictactoe_reset_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Game", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Options: Difficulty & Side Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Difficulty", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Easy", "Medium", "Unbeatable").forEach { diff ->
                        FilterChip(
                            selected = uiState.ticTacToeDifficulty == diff,
                            onClick = { gamesViewModel.setTicTacToeDifficulty(diff) },
                            label = { Text(diff, fontSize = 10.sp) },
                            modifier = Modifier.testTag("tictactoe_diff_$diff")
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Play As", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("X", "O").forEach { sym ->
                        FilterChip(
                            selected = uiState.playerSymbol == sym,
                            onClick = { gamesViewModel.setPlayerSymbol(sym) },
                            label = { Text(sym, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tictactoe_symbol_$sym")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3x3 Grid
        Column(
            modifier = Modifier
                .width(300.dp)
                .height(300.dp)
                .border(2.dp, Color(0xFFD0BCFF), RoundedCornerShape(14.dp))
        ) {
            for (r in 0 until 3) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until 3) {
                        val idx = r * 3 + c
                        val mark = uiState.ticTacToeBoard[idx]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(1.dp, Color.White.copy(alpha = 0.15f))
                                .background(Color(0xFF1E1938))
                                .clickable { gamesViewModel.makeTicTacToeMove(idx) }
                                .testTag("tictactoe_cell_$idx"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mark,
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mark == "X") Color(0xFF4FD8EB) else Color(0xFFFFB4A2)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Score Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wins", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("${uiState.tictactoeWins}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Losses", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("${uiState.tictactoeLosses}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB4A2))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Draws", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("${uiState.tictactoeDraws}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { gamesViewModel.resetTicTacToe() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("tictactoe_new_match_button")
        ) {
            Text("New Match", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold)
        }
    }
}
