package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.ChessPiece
import com.example.ui.viewmodels.GamesViewModel

@Composable
fun ChessGameScreen(
    gamesViewModel: GamesViewModel
) {
    val uiState by gamesViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .padding(16.dp)
            .testTag("chess_screen_container")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Chess vs Game Mind AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = when {
                        uiState.isChessAiThinking -> "AI is analyzing moves..."
                        uiState.chessStatus == "CHECKMATE" -> "Checkmate! ${if (uiState.chessTurn == "w") "Black" else "White"} Wins"
                        uiState.chessStatus == "STALEMATE" -> "Stalemate - Draw Game!"
                        uiState.chessTurn == uiState.playerColor -> "Your turn (White)"
                        else -> "Game Mind AI turn (Black)"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFFFB4A2)
                )
            }
            Row {
                IconButton(onClick = { gamesViewModel.askAiCoach("Chess") }, modifier = Modifier.testTag("chess_ai_coach_button")) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Advice", tint = Color(0xFF4FD8EB))
                }
                IconButton(onClick = { gamesViewModel.undoChessMove() }, modifier = Modifier.testTag("chess_undo_button")) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo Move", tint = Color.White)
                }
                IconButton(onClick = { gamesViewModel.resetChess() }, modifier = Modifier.testTag("chess_reset_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Match", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Difficulty Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Difficulty:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            listOf("Easy", "Medium", "Hard").forEach { diff ->
                FilterChip(
                    selected = uiState.chessDifficulty == diff,
                    onClick = { gamesViewModel.setChessDifficulty(diff) },
                    label = { Text(diff, fontSize = 11.sp) },
                    modifier = Modifier.testTag("chess_diff_$diff")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8x8 Interactive Chess Board UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, Color(0xFFFFB4A2), RoundedCornerShape(8.dp))
        ) {
            for (r in 0 until 8) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until 8) {
                        val pos = Pair(r, c)
                        val isDark = (r + c) % 2 != 0
                        val isSelected = uiState.selectedSquare == pos
                        val isValidMove = pos in uiState.validMoves
                        val piece = uiState.chessBoard[pos]

                        val cellColor = when {
                            isSelected -> Color(0xFF4FD8EB).copy(alpha = 0.8f)
                            isValidMove -> Color(0xFFD0BCFF).copy(alpha = 0.6f)
                            isDark -> Color(0xFF2A1B54)
                            else -> Color(0xFF3D2C70)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(cellColor)
                                .clickable { gamesViewModel.selectChessSquare(r, c) }
                                .testTag("chess_square_${r}_$c"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidMove && piece == null) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4FD8EB))
                                )
                            }
                            if (piece != null) {
                                Text(
                                    text = getPieceUnicode(piece),
                                    fontSize = 24.sp,
                                    color = if (piece.isWhite) Color.White else Color(0xFFFFB4A2)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Move Log & Captured Pieces
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Move Notation Log", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("White: ${uiState.blackCaptured.size} caps | Black: ${uiState.whiteCaptured.size} caps", fontSize = 11.sp, color = Color(0xFFD0BCFF))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                items(uiState.chessMoveLog) { log ->
                    Text(log, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

private fun getPieceUnicode(piece: ChessPiece): String {
    return when (piece.type) {
        "K" -> if (piece.isWhite) "♔" else "♚"
        "Q" -> if (piece.isWhite) "♕" else "♛"
        "R" -> if (piece.isWhite) "♖" else "♜"
        "B" -> if (piece.isWhite) "♗" else "♝"
        "N" -> if (piece.isWhite) "♘" else "♞"
        "P" -> if (piece.isWhite) "♙" else "♟"
        else -> ""
    }
}
