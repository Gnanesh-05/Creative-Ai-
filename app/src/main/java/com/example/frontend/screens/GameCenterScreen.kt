package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.frontend.components.GlassCard
import com.example.frontend.theme.IridescentGradient
import com.example.frontend.theme.LightCanvasEnd
import com.example.frontend.theme.LightCanvasMid
import com.example.frontend.theme.LightCanvasStart
import com.example.frontend.theme.NexusMagenta
import com.example.frontend.theme.NexusViolet
import com.example.frontend.theme.PastelCyan
import com.example.frontend.theme.PastelGold
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

@Composable
fun GameCenterScreen(
    onBack: () -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedGameTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0: Tic Tac Toe, 1: Chess AI, 2: Mind Maze

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightCanvasStart,
                        LightCanvasMid,
                        LightCanvasEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryLight
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Games",
                        tint = NexusViolet,
                        modifier = Modifier
                            .size(26.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "AI GAME CENTER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 2.sp,
                            color = TextPrimaryLight
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IridescentGradient)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "MIND AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3D Banner graphic header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color.White, RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_game_center_banner),
                    contentDescription = "AI Game Center Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.65f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "MIND AI GAMES",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Real-time AI logic • Challenge Tic Tac Toe, Chess & Mind Maze",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .border(1.dp, Color.White, RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf(
                    Triple(0, "Tic-Tac-Toe", Icons.Default.GridOn),
                    Triple(1, "Chess AI", Icons.Default.Extension),
                    Triple(2, "Mind Maze", Icons.Default.AutoAwesome)
                )

                tabs.forEach { (index, title, icon) ->
                    val isSelected = selectedGameTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(NexusViolet, NexusMagenta)
                                ) else Brush.linearGradient(
                                    listOf(Color.Transparent, Color.Transparent)
                                )
                            )
                            .clickable { selectedGameTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) Color.White else TextSecondaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimaryLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Area Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedGameTab) {
                    0 -> TicTacToeGameView()
                    1 -> ChessGameView()
                    2 -> MazeGameView()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// 1. TIC TAC TOE GAME VS SMART MIND AI
// -----------------------------------------------------------------------------
@Composable
fun TicTacToeGameView() {
    var board by remember { mutableStateOf(Array(9) { "" }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) } // "X", "O", "Draw"
    var winningLine by remember { mutableStateOf<List<Int>?>(null) }
    var isAiThinking by remember { mutableStateOf(false) }

    var playerScore by remember { mutableIntStateOf(0) }
    var aiScore by remember { mutableIntStateOf(0) }
    var drawScore by remember { mutableIntStateOf(0) }
    var difficulty by remember { mutableStateOf("Smart Mind AI") } // "Easy", "Smart Mind AI", "Unbeatable"

    val coroutineScope = rememberCoroutineScope()

    fun checkWinner(b: Array<String>): Pair<String?, List<Int>?> {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (pattern in winPatterns) {
            val (a, c1, c2) = pattern
            if (b[a].isNotEmpty() && b[a] == b[c1] && b[a] == b[c2]) {
                return Pair(b[a], pattern)
            }
        }

        if (b.all { it.isNotEmpty() }) {
            return Pair("Draw", null)
        }

        return Pair(null, null)
    }

    fun makeAiMove() {
        if (winner != null || isPlayerTurn) return

        coroutineScope.launch {
            isAiThinking = true
            delay(350)

            val emptyIndices = board.indices.filter { board[it].isEmpty() }
            if (emptyIndices.isNotEmpty()) {
                val targetIndex = when (difficulty) {
                    "Easy" -> emptyIndices.random()
                    "Unbeatable" -> getMinimaxBestMove(board.copyOf())
                    else -> getSmartAiMove(board.copyOf(), emptyIndices)
                }

                val newBoard = board.copyOf()
                newBoard[targetIndex] = "O"
                board = newBoard

                val (resWinner, resLine) = checkWinner(board)
                if (resWinner != null) {
                    winner = resWinner
                    winningLine = resLine
                    if (resWinner == "O") aiScore++
                    else if (resWinner == "Draw") drawScore++
                } else {
                    isPlayerTurn = true
                }
            }
            isAiThinking = false
        }
    }

    fun onCellClick(index: Int) {
        if (board[index].isNotEmpty() || winner != null || !isPlayerTurn || isAiThinking) return

        val newBoard = board.copyOf()
        newBoard[index] = "X"
        board = newBoard

        val (resWinner, resLine) = checkWinner(board)
        if (resWinner != null) {
            winner = resWinner
            winningLine = resLine
            if (resWinner == "X") playerScore++
            else if (resWinner == "Draw") drawScore++
        } else {
            isPlayerTurn = false
            makeAiMove()
        }
    }

    fun resetBoard() {
        board = Array(9) { "" }
        winner = null
        winningLine = null
        isPlayerTurn = true
        isAiThinking = false
    }

    GlassCard(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Difficulty Selector & Scoreboard
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIFFICULTY:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryLight
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Easy", "Smart Mind AI", "Unbeatable").forEach { level ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (difficulty == level) NexusViolet else Color.White.copy(alpha = 0.8f)
                                    )
                                    .clickable {
                                        difficulty = level
                                        resetBoard()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = level,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (difficulty == level) Color.White else TextPrimaryLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scoreboard Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreBadge("YOU (X)", playerScore, Color(0xFF3B82F6))
                    ScoreBadge("DRAWS", drawScore, Color(0xFF6B7280))
                    ScoreBadge("MIND AI (O)", aiScore, Color(0xFFEC4899))
                }
            }

            // Game Turn Banner / Result Alert
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when (winner) {
                            "X" -> Color(0xFF10B981)
                            "O" -> Color(0xFFEF4444)
                            "Draw" -> Color(0xFFF59E0B)
                            else -> Color.White.copy(alpha = 0.85f)
                        }
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        winner == "X" -> "🎉 YOU WON THE AI!"
                        winner == "O" -> "🤖 MIND AI DEFEATED YOU!"
                        winner == "Draw" -> "🤝 IT'S A DRAW GAME!"
                        isAiThinking -> "🧠 Mind AI is calculating move..."
                        isPlayerTurn -> "Your Turn (X)"
                        else -> "Mind AI Turn (O)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (winner != null) Color.White else TextPrimaryLight
                )
            }

            // 3x3 Tic Tac Toe Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (r in 0..2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (c in 0..2) {
                                val index = r * 3 + c
                                val symbol = board[index]
                                val isWinCell = winningLine?.contains(index) == true

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            when {
                                                isWinCell -> Color(0xFFFDE047)
                                                symbol == "X" -> Color(0xFFEFF6FF)
                                                symbol == "O" -> Color(0xFFFDF2F8)
                                                else -> Color.White.copy(alpha = 0.9f)
                                            }
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isWinCell) Color(0xFFEAB308) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onCellClick(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (symbol.isNotEmpty()) {
                                        Text(
                                            text = symbol,
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (symbol == "X") Color(0xFF2563EB) else Color(0xFFDB2777)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { resetBoard() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexusViolet)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restart Board", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getSmartAiMove(b: Array<String>, emptyIndices: List<Int>): Int {
    // 1. Can AI win on this turn?
    for (i in emptyIndices) {
        val testBoard = b.copyOf()
        testBoard[i] = "O"
        if (checkWinSymbol(testBoard, "O")) return i
    }
    // 2. Must AI block Player from winning on next turn?
    for (i in emptyIndices) {
        val testBoard = b.copyOf()
        testBoard[i] = "X"
        if (checkWinSymbol(testBoard, "X")) return i
    }
    // 3. Take center if available
    if (b[4].isEmpty()) return 4
    // 4. Take corners
    val corners = listOf(0, 2, 6, 8).filter { b[it].isEmpty() }
    if (corners.isNotEmpty()) return corners.random()

    return emptyIndices.random()
}

private fun getMinimaxBestMove(b: Array<String>): Int {
    var bestScore = Int.MIN_VALUE
    var bestMove = -1

    for (i in b.indices) {
        if (b[i].isEmpty()) {
            b[i] = "O"
            val score = minimax(b, 0, false)
            b[i] = ""
            if (score > bestScore) {
                bestScore = score
                bestMove = i
            }
        }
    }
    return if (bestMove != -1) bestMove else b.indices.first { b[it].isEmpty() }
}

private fun minimax(b: Array<String>, depth: Int, isMaximizing: Boolean): Int {
    if (checkWinSymbol(b, "O")) return 10 - depth
    if (checkWinSymbol(b, "X")) return depth - 10
    if (b.all { it.isNotEmpty() }) return 0

    if (isMaximizing) {
        var bestScore = Int.MIN_VALUE
        for (i in b.indices) {
            if (b[i].isEmpty()) {
                b[i] = "O"
                val score = minimax(b, depth + 1, false)
                b[i] = ""
                bestScore = maxOf(score, bestScore)
            }
        }
        return bestScore
    } else {
        var bestScore = Int.MAX_VALUE
        for (i in b.indices) {
            if (b[i].isEmpty()) {
                b[i] = "X"
                val score = minimax(b, depth + 1, true)
                b[i] = ""
                bestScore = minOf(score, bestScore)
            }
        }
        return bestScore
    }
}

private fun checkWinSymbol(b: Array<String>, sym: String): Boolean {
    val winPatterns = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    return winPatterns.any { (a, c1, c2) -> b[a] == sym && b[c1] == sym && b[c2] == sym }
}

@Composable
fun ScoreBadge(title: String, score: Int, badgeColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeColor.copy(alpha = 0.12f))
            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
        Text(text = "$score", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = badgeColor)
    }
}

// -----------------------------------------------------------------------------
// 2. REAL-TIME CHESS GAME VS MIND AI
// -----------------------------------------------------------------------------
data class ChessPiece(val type: String, val isWhite: Boolean) // "P","R","N","B","Q","K"

@Composable
fun ChessGameView() {
    var boardState by remember { mutableStateOf(initialChessBoard()) }
    var selectedSquare by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var validMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var isWhiteTurn by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("Your Turn (White)") }
    var isAiThinking by remember { mutableStateOf(false) }

    var whiteCaptured by remember { mutableIntStateOf(0) }
    var blackCaptured by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    fun triggerAiMove() {
        if (isWhiteTurn) return

        coroutineScope.launch {
            isAiThinking = true
            statusText = "🧠 Mind AI analyzing position..."
            delay(500)

            val aiMove = calculateAiChessMove(boardState)
            if (aiMove != null) {
                val (from, to) = aiMove
                val newBoard = Array(8) { r -> Array(8) { c -> boardState[r][c] } }

                val captured = newBoard[to.first][to.second]
                if (captured != null && captured.isWhite) {
                    whiteCaptured++
                }

                newBoard[to.first][to.second] = newBoard[from.first][from.second]
                newBoard[from.first][from.second] = null

                boardState = newBoard
                isWhiteTurn = true
                statusText = "Your Turn (White)"
            } else {
                statusText = "Checkmate / Stalemate!"
            }
            isAiThinking = false
        }
    }

    fun onSquareClick(r: Int, c: Int) {
        if (!isWhiteTurn || isAiThinking) return

        val piece = boardState[r][c]
        val sel = selectedSquare

        if (sel != null && validMoves.contains(Pair(r, c))) {
            // Move piece
            val newBoard = Array(8) { row -> Array(8) { col -> boardState[row][col] } }
            val captured = newBoard[r][c]
            if (captured != null && !captured.isWhite) {
                blackCaptured++
            }

            newBoard[r][c] = newBoard[sel.first][sel.second]
            newBoard[sel.first][sel.second] = null

            // Pawn promotion
            if (newBoard[r][c]?.type == "P" && r == 0) {
                newBoard[r][c] = ChessPiece("Q", true)
            }

            boardState = newBoard
            selectedSquare = null
            validMoves = emptyList()
            isWhiteTurn = false
            triggerAiMove()
        } else if (piece != null && piece.isWhite) {
            selectedSquare = Pair(r, c)
            validMoves = getValidMovesForPiece(r, c, boardState)
        } else {
            selectedSquare = null
            validMoves = emptyList()
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Chess Header & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MIND CHESS ENGINE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = if (isWhiteTurn) NexusViolet else NexusMagenta,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "AI Captures: $whiteCaptured", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "You Captured: $blackCaptured", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 8x8 Chessboard Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(2.5.dp, Color.White, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0..7) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (c in 0..7) {
                                val isLightSquare = (r + c) % 2 == 0
                                val isSelected = selectedSquare?.first == r && selectedSquare?.second == c
                                val isValidDest = validMoves.contains(Pair(r, c))
                                val piece = boardState[r][c]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            when {
                                                isSelected -> Color(0xFFFDE047)
                                                isValidDest -> Color(0xFF86EFAC)
                                                isLightSquare -> Color(0xFFF1F5F9)
                                                else -> Color(0xFF94A3B8)
                                            }
                                        )
                                        .clickable { onSquareClick(r, c) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (piece != null) {
                                        Text(
                                            text = getChessSymbol(piece),
                                            fontSize = 24.sp,
                                            color = if (piece.isWhite) Color.White else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (isValidDest && piece == null) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF16A34A))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Game Control Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        boardState = initialChessBoard()
                        selectedSquare = null
                        validMoves = emptyList()
                        isWhiteTurn = true
                        statusText = "Your Turn (White)"
                        whiteCaptured = 0
                        blackCaptured = 0
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexusViolet)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "New Game", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chess Match", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

private fun getChessSymbol(p: ChessPiece): String {
    return when (p.type) {
        "K" -> if (p.isWhite) "♔" else "♚"
        "Q" -> if (p.isWhite) "♕" else "♛"
        "R" -> if (p.isWhite) "♖" else "♜"
        "B" -> if (p.isWhite) "♗" else "♝"
        "N" -> if (p.isWhite) "♘" else "♞"
        "P" -> if (p.isWhite) "♙" else "♟"
        else -> ""
    }
}

private fun initialChessBoard(): Array<Array<ChessPiece?>> {
    val b = Array(8) { Array<ChessPiece?>(8) { null } }
    val mainRow = listOf("R", "N", "B", "Q", "K", "B", "N", "R")

    // Black pieces (Top: row 0 & 1)
    for (c in 0..7) {
        b[0][c] = ChessPiece(mainRow[c], false)
        b[1][c] = ChessPiece("P", false)
    }

    // White pieces (Bottom: row 6 & 7)
    for (c in 0..7) {
        b[6][c] = ChessPiece("P", true)
        b[7][c] = ChessPiece(mainRow[c], true)
    }

    return b
}

private fun getValidMovesForPiece(r: Int, c: Int, board: Array<Array<ChessPiece?>>): List<Pair<Int, Int>> {
    val piece = board[r][c] ?: return emptyList()
    val moves = mutableListOf<Pair<Int, Int>>()
    val isWhite = piece.isWhite

    when (piece.type) {
        "P" -> {
            val dir = if (isWhite) -1 else 1
            val nextR = r + dir
            if (nextR in 0..7 && board[nextR][c] == null) {
                moves.add(Pair(nextR, c))
                val startRow = if (isWhite) 6 else 1
                val doubleR = r + 2 * dir
                if (r == startRow && board[doubleR][c] == null) {
                    moves.add(Pair(doubleR, c))
                }
            }
            // Pawn captures
            for (dc in listOf(-1, 1)) {
                val capC = c + dc
                if (nextR in 0..7 && capC in 0..7) {
                    val target = board[nextR][capC]
                    if (target != null && target.isWhite != isWhite) {
                        moves.add(Pair(nextR, capC))
                    }
                }
            }
        }
        "N" -> {
            val offsets = listOf(
                Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
            )
            for ((dr, dc) in offsets) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0..7 && nc in 0..7) {
                    val target = board[nr][nc]
                    if (target == null || target.isWhite != isWhite) {
                        moves.add(Pair(nr, nc))
                    }
                }
            }
        }
        "K" -> {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val nr = r + dr
                    val nc = c + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val target = board[nr][nc]
                        if (target == null || target.isWhite != isWhite) {
                            moves.add(Pair(nr, nc))
                        }
                    }
                }
            }
        }
        "R", "B", "Q" -> {
            val directions = mutableListOf<Pair<Int, Int>>()
            if (piece.type == "R" || piece.type == "Q") {
                directions.addAll(listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)))
            }
            if (piece.type == "B" || piece.type == "Q") {
                directions.addAll(listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
            }

            for ((dr, dc) in directions) {
                var nr = r + dr
                var nc = c + dc
                while (nr in 0..7 && nc in 0..7) {
                    val target = board[nr][nc]
                    if (target == null) {
                        moves.add(Pair(nr, nc))
                    } else {
                        if (target.isWhite != isWhite) {
                            moves.add(Pair(nr, nc))
                        }
                        break
                    }
                    nr += dr
                    nc += dc
                }
            }
        }
    }
    return moves
}

private fun calculateAiChessMove(board: Array<Array<ChessPiece?>>): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    val allAiPieces = mutableListOf<Pair<Int, Int>>()
    for (r in 0..7) {
        for (c in 0..7) {
            val piece = board[r][c]
            if (piece != null && !piece.isWhite) {
                allAiPieces.add(Pair(r, c))
            }
        }
    }

    if (allAiPieces.isEmpty()) return null

    val possibleMoves = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
    val captureMoves = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()

    for (src in allAiPieces) {
        val dests = getValidMovesForPiece(src.first, src.second, board)
        for (dst in dests) {
            val movePair = Pair(src, dst)
            possibleMoves.add(movePair)
            val target = board[dst.first][dst.second]
            if (target != null && target.isWhite) {
                captureMoves.add(movePair)
            }
        }
    }

    // AI prefers capture moves if available
    return if (captureMoves.isNotEmpty()) {
        captureMoves.random()
    } else if (possibleMoves.isNotEmpty()) {
        possibleMoves.random()
    } else null
}

// -----------------------------------------------------------------------------
// 3. MIND MAZE GAME WITH REAL-TIME AI SOLVER BOT & ULTRA-FAST CONTROLS
// -----------------------------------------------------------------------------
@Composable
fun MazeGameView() {
    var mazeSize by remember { mutableIntStateOf(7) } // 5x5, 7x7, 9x9
    var grid by remember { mutableStateOf(generateMazeGrid(mazeSize)) }
    var playerPos by remember { mutableStateOf(Pair(0, 0)) }
    var aiPos by remember { mutableStateOf(Pair(0, 0)) }
    var goalPos by remember { mutableStateOf(Pair(mazeSize - 1, mazeSize - 1)) }
    
    var stepCount by remember { mutableIntStateOf(0) }
    var hasWon by remember { mutableStateOf(false) }
    var aiWinner by remember { mutableStateOf(false) }
    var aiPath by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var playerTrail by remember { mutableStateOf<Set<Pair<Int, Int>>>(setOf(Pair(0, 0))) }
    var isAiRunning by remember { mutableStateOf(false) }
    var isAutoMoving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun resetMaze(newSize: Int = mazeSize) {
        mazeSize = newSize
        grid = generateMazeGrid(newSize)
        playerPos = Pair(0, 0)
        aiPos = Pair(0, 0)
        goalPos = Pair(newSize - 1, newSize - 1)
        stepCount = 0
        hasWon = false
        aiWinner = false
        aiPath = emptyList()
        playerTrail = setOf(Pair(0, 0))
        isAiRunning = false
        isAutoMoving = false
    }

    fun movePlayer(dr: Int, dc: Int) {
        if (hasWon || aiWinner) return
        val nr = playerPos.first + dr
        val nc = playerPos.second + dc

        if (nr in 0 until mazeSize && nc in 0 until mazeSize && !grid[nr][nc]) {
            playerPos = Pair(nr, nc)
            playerTrail = playerTrail + Pair(nr, nc)
            stepCount++
            if (playerPos == goalPos) {
                hasWon = true
            }
        }
    }

    // Tap-to-move BFS auto pathfinding: tap any tile on grid to walk there instantly!
    fun tapToMoveToTile(targetR: Int, targetC: Int) {
        if (hasWon || aiWinner || isAutoMoving || grid[targetR][targetC]) return
        if (playerPos == Pair(targetR, targetC)) return

        val path = solveMazeBFS(grid, playerPos, Pair(targetR, targetC))
        if (path.size > 1) {
            coroutineScope.launch {
                isAutoMoving = true
                // Drop current position, move along remaining path
                for (step in path.drop(1)) {
                    if (hasWon || aiWinner) break
                    playerPos = step
                    playerTrail = playerTrail + step
                    stepCount++
                    if (playerPos == goalPos) {
                        hasWon = true
                        break
                    }
                    delay(50) // Fast 50ms animation per tile step
                }
                isAutoMoving = false
            }
        }
    }

    fun startAiSolver() {
        if (isAiRunning || hasWon || aiWinner) return

        coroutineScope.launch {
            isAiRunning = true
            val path = solveMazeBFS(grid, aiPos, goalPos)
            aiPath = path

            for (node in path) {
                if (hasWon) break
                delay(180) // Fast 180ms per AI step for exciting race
                aiPos = node
                if (aiPos == goalPos && !hasWon) {
                    aiWinner = true
                    break
                }
            }
            isAiRunning = false
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & Level Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MIND MAZE AI", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimaryLight)
                    Text(
                        text = when {
                            hasWon -> "🎉 ESCAPED IN $stepCount STEPS!"
                            aiWinner -> "🤖 AI SOLVER OUTRAN YOU!"
                            isAiRunning -> "⚡ AI Pathfinder Racing..."
                            else -> "Steps: $stepCount • Tap tile to auto-move"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasWon) Color(0xFF10B981) else if (aiWinner) NexusMagenta else NexusViolet
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(5 to "Easy", 7 to "Med", 9 to "Hard").forEach { (sz, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (mazeSize == sz) NexusViolet else Color.White.copy(alpha = 0.8f)
                                )
                                .clickable { resetMaze(sz) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mazeSize == sz) Color.White else TextPrimaryLight
                            )
                        }
                    }
                }
            }

            // Maze Grid Box with Gesture & Tap Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A)) // Dark slate cyberpunk canvas
                    .border(2.dp, NexusViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(6.dp)
                    .pointerInput(mazeSize, hasWon, aiWinner) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if (abs(x) > abs(y)) {
                                if (x > 12) movePlayer(0, 1) else if (x < -12) movePlayer(0, -1)
                            } else {
                                if (y > 12) movePlayer(1, 0) else if (y < -12) movePlayer(-1, 0)
                            }
                        }
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until mazeSize) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (c in 0 until mazeSize) {
                                val isWall = grid[r][c]
                                val isPlayer = playerPos.first == r && playerPos.second == c
                                val isAi = aiPos.first == r && aiPos.second == c
                                val isGoal = goalPos.first == r && goalPos.second == c
                                val isAiPath = aiPath.contains(Pair(r, c))
                                val isTraveled = playerTrail.contains(Pair(r, c))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.5.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                isWall -> Color(0xFF1E293B)
                                                isPlayer -> Color(0xFF38BDF8)
                                                isAi -> Color(0xFFF43F5E)
                                                isGoal -> Color(0xFF10B981)
                                                isAiPath -> Color(0xFFFACC15).copy(alpha = 0.45f)
                                                isTraveled -> Color(0xFF0EA5E9).copy(alpha = 0.25f)
                                                else -> Color(0xFF334155)
                                            }
                                        )
                                        .clickable(enabled = !isWall) {
                                            tapToMoveToTile(r, c)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlayer) {
                                        Text("🚀", fontSize = if (mazeSize == 9) 12.sp else 16.sp)
                                    } else if (isAi) {
                                        Text("🤖", fontSize = if (mazeSize == 9) 12.sp else 16.sp)
                                    } else if (isGoal) {
                                        Text("💎", fontSize = if (mazeSize == 9) 12.sp else 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions & Fast D-Pad Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Buttons (Race AI & New Maze)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { startAiSolver() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NexusMagenta),
                        enabled = !isAiRunning && !hasWon && !aiWinner
                    ) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Race AI", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Race AI Bot", fontSize = 11.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { resetMaze() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Maze", fontSize = 11.sp)
                    }
                }

                // Ultra Fast D-Pad
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { movePlayer(-1, 0) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = TextPrimaryLight)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(
                            onClick = { movePlayer(0, -1) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Left", tint = TextPrimaryLight)
                        }

                        IconButton(
                            onClick = { movePlayer(0, 1) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Right", tint = TextPrimaryLight)
                        }
                    }

                    IconButton(
                        onClick = { movePlayer(1, 0) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = TextPrimaryLight)
                    }
                }
            }
        }
    }
}

private fun generateMazeGrid(size: Int): Array<BooleanArray> {
    val grid = Array(size) { BooleanArray(size) { true } }

    fun carve(r: Int, c: Int) {
        grid[r][c] = false
        val dirs = listOf(Pair(-2, 0), Pair(2, 0), Pair(0, -2), Pair(0, 2)).shuffled()
        for ((dr, dc) in dirs) {
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until size && nc in 0 until size && grid[nr][nc]) {
                grid[r + dr / 2][c + dc / 2] = false
                carve(nr, nc)
            }
        }
    }

    carve(0, 0)
    grid[0][0] = false
    grid[size - 1][size - 1] = false
    grid[size - 2][size - 1] = false
    grid[size - 1][size - 2] = false

    return grid
}

private fun solveMazeBFS(grid: Array<BooleanArray>, start: Pair<Int, Int>, goal: Pair<Int, Int>): List<Pair<Int, Int>> {
    val n = grid.size
    val queue: Queue<List<Pair<Int, Int>>> = LinkedList()
    val visited = Array(n) { BooleanArray(n) }

    queue.add(listOf(start))
    visited[start.first][start.second] = true

    while (queue.isNotEmpty()) {
        val path = queue.poll() ?: break
        val curr = path.last()

        if (curr == goal) return path

        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        for ((dr, dc) in dirs) {
            val nr = curr.first + dr
            val nc = curr.second + dc

            if (nr in 0 until n && nc in 0 until n && !grid[nr][nc] && !visited[nr][nc]) {
                visited[nr][nc] = true
                val newPath = path.toMutableList().apply { add(Pair(nr, nc)) }
                queue.add(newPath)
            }
        }
    }
    return emptyList()
}
