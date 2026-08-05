package com.example.frontend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend.remote.TicTacToeMoveRequest
import com.example.backend.repository.CreativeAiRepository
import com.example.domain.engine.GameEngineHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChessPiece(
    val type: String, // "P", "N", "B", "R", "Q", "K"
    val isWhite: Boolean
)

data class GameHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val gameType: String, // "Chess", "Tic-Tac-Toe", "AI Maze"
    val result: String, // "Won", "Lost", "Draw"
    val score: Int,
    val totalMoves: Int,
    val dateStr: String = "Just now"
)

data class GamesUiState(
    // Active Tab in Game Mind Hub
    val selectedTab: Int = 0, // 0 = Games Hub, 1 = History, 2 = Statistics, 3 = Profile & Preferences

    // --- Chess State ---
    val chessBoard: Map<Pair<Int, Int>, ChessPiece> = defaultChessBoard(),
    val chessFen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    val chessTurn: String = "w", // "w" or "b"
    val playerColor: String = "w", // "w" or "b"
    val chessDifficulty: String = "Medium", // Easy, Medium, Hard
    val selectedSquare: Pair<Int, Int>? = null,
    val validMoves: List<Pair<Int, Int>> = emptyList(),
    val chessMoveLog: List<String> = listOf("Match initialized vs Game Mind AI"),
    val whiteCaptured: List<String> = emptyList(),
    val blackCaptured: List<String> = emptyList(),
    val isChessCheck: Boolean = false,
    val chessStatus: String = "IN_PROGRESS", // "IN_PROGRESS", "CHECKMATE", "STALEMATE", "RESIGNED"
    val chessWinner: String? = null,
    val isChessAiThinking: Boolean = false,
    val chessMoveHistoryStack: List<Map<Pair<Int, Int>, ChessPiece>> = emptyList(),

    // --- Tic-Tac-Toe State ---
    val ticTacToeBoard: List<String> = List(9) { "" },
    val ticTacToeWinner: String? = null, // "X", "O", "DRAW", null
    val ticTacToeDifficulty: String = "Unbeatable", // Easy, Medium, Unbeatable
    val playerSymbol: String = "X", // "X" or "O"
    val isTicTacToeLoading: Boolean = false,
    val tictactoeWins: Int = 0,
    val tictactoeLosses: Int = 0,
    val tictactoeDraws: Int = 0,

    // --- Maze State ---
    val mazeSize: Int = 11, // 7, 11, 15
    val mazeGrid: List<List<Int>> = emptyList(), // 0 = path, 1 = wall
    val playerPos: Pair<Int, Int> = Pair(1, 1),
    val startPos: Pair<Int, Int> = Pair(1, 1),
    val endPos: Pair<Int, Int> = Pair(9, 9),
    val solutionPath: List<Pair<Int, Int>> = emptyList(),
    val isMazeSolved: Boolean = false,
    val mazeStepCount: Int = 0,
    val mazeTimerSeconds: Int = 0,
    val isRaceMode: Boolean = false,
    val aiRacePos: Pair<Int, Int> = Pair(1, 1),

    // --- Preferences & History ---
    val gameHistory: List<GameHistoryRecord> = listOf(
        GameHistoryRecord(gameType = "Chess", result = "Won", score = 1200, totalMoves = 24, dateStr = "Today"),
        GameHistoryRecord(gameType = "Tic-Tac-Toe", result = "Won", score = 300, totalMoves = 5, dateStr = "Yesterday"),
        GameHistoryRecord(gameType = "AI Maze", result = "Won", score = 850, totalMoves = 18, dateStr = "2 days ago")
    ),
    val soundEffectsEnabled: Boolean = true,
    val aiCoachPersonality: String = "Grandmaster Tactician",

    // --- LLM AI Coach Dialog ---
    val aiCoachDialogTitle: String? = null,
    val aiCoachMessage: String? = null,
    val isAiCoachLoading: Boolean = false
)

private fun defaultChessBoard(): Map<Pair<Int, Int>, ChessPiece> {
    val b = mutableMapOf<Pair<Int, Int>, ChessPiece>()
    val backRow = listOf("R", "N", "B", "Q", "K", "B", "N", "R")
    for (c in 0..7) {
        b[Pair(0, c)] = ChessPiece(backRow[c], isWhite = false)
        b[Pair(1, c)] = ChessPiece("P", isWhite = false)
        b[Pair(6, c)] = ChessPiece("P", isWhite = true)
        b[Pair(7, c)] = ChessPiece(backRow[c], isWhite = true)
    }
    return b
}

class GamesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    private var mazeTimerJob: Job? = null

    init {
        generateMaze(11)
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    // --- Tic-Tac-Toe Methods ---
    fun setTicTacToeDifficulty(diff: String) {
        _uiState.value = _uiState.value.copy(ticTacToeDifficulty = diff)
        resetTicTacToe()
    }

    fun setPlayerSymbol(symbol: String) {
        _uiState.value = _uiState.value.copy(playerSymbol = symbol)
        resetTicTacToe()
    }

    fun makeTicTacToeMove(index: Int) {
        val state = _uiState.value
        val currentBoard = state.ticTacToeBoard.toMutableList()
        if (index !in currentBoard.indices || currentBoard[index].isNotEmpty() || state.ticTacToeWinner != null || state.isTicTacToeLoading) {
            return
        }

        val pSym = state.playerSymbol
        val aiSym = if (pSym == "X") "O" else "X"

        currentBoard[index] = pSym
        val winnerAfterUser = GameEngineHelper.checkTicTacToeWinner(currentBoard)

        if (winnerAfterUser != null) {
            handleTicTacToeFinish(currentBoard, winnerAfterUser, pSym)
            return
        }

        _uiState.value = state.copy(ticTacToeBoard = currentBoard, isTicTacToeLoading = true)

        viewModelScope.launch {
            delay(400) // AI response pulse
            val aiMove = GameEngineHelper.getOptimalTicTacToeMove(currentBoard, state.ticTacToeDifficulty, aiSym)
            if (aiMove in currentBoard.indices && currentBoard[aiMove].isEmpty()) {
                currentBoard[aiMove] = aiSym
            }
            val finalWinner = GameEngineHelper.checkTicTacToeWinner(currentBoard)
            if (finalWinner != null) {
                handleTicTacToeFinish(currentBoard, finalWinner, pSym)
            } else {
                _uiState.value = _uiState.value.copy(ticTacToeBoard = currentBoard, isTicTacToeLoading = false)
            }
        }
    }

    private fun handleTicTacToeFinish(board: List<String>, winner: String, playerSymbol: String) {
        val current = _uiState.value
        val isUserWin = (winner == playerSymbol)
        val isDraw = (winner == "DRAW")

        val newWins = if (isUserWin) current.tictactoeWins + 1 else current.tictactoeWins
        val newLosses = if (!isUserWin && !isDraw) current.tictactoeLosses + 1 else current.tictactoeLosses
        val newDraws = if (isDraw) current.tictactoeDraws + 1 else current.tictactoeDraws

        val resText = if (isUserWin) "Won" else (if (isDraw) "Draw" else "Lost")
        val newRecord = GameHistoryRecord(
            gameType = "Tic-Tac-Toe",
            result = resText,
            score = if (isUserWin) 300 else (if (isDraw) 100 else 0),
            totalMoves = board.count { it.isNotEmpty() }
        )

        _uiState.value = current.copy(
            ticTacToeBoard = board,
            ticTacToeWinner = winner,
            isTicTacToeLoading = false,
            tictactoeWins = newWins,
            tictactoeLosses = newLosses,
            tictactoeDraws = newDraws,
            gameHistory = listOf(newRecord) + current.gameHistory
        )

        viewModelScope.launch {
            repository.saveHistory("GAME_TICTACTOE", "Tic-Tac-Toe Match", "Result: $resText vs Minimax AI")
        }
    }

    fun resetTicTacToe() {
        _uiState.value = _uiState.value.copy(
            ticTacToeBoard = List(9) { "" },
            ticTacToeWinner = null,
            isTicTacToeLoading = false
        )
    }

    // --- Chess Methods ---
    fun selectChessSquare(row: Int, col: Int) {
        val state = _uiState.value
        if (state.chessStatus != "IN_PROGRESS" || state.isChessAiThinking) return

        val pos = Pair(row, col)
        val piece = state.chessBoard[pos]
        val isWhiteTurn = (state.chessTurn == "w")
        val isPlayerTurn = (state.chessTurn == state.playerColor)

        if (!isPlayerTurn) return

        if (state.selectedSquare == null) {
            if (piece != null && piece.isWhite == isWhiteTurn) {
                val targets = computeValidMovesForPiece(pos, piece, state.chessBoard)
                _uiState.value = state.copy(selectedSquare = pos, validMoves = targets)
            }
        } else {
            val fromPos = state.selectedSquare
            if (fromPos == pos) {
                _uiState.value = state.copy(selectedSquare = null, validMoves = emptyList())
            } else if (pos in state.validMoves) {
                executeChessMove(fromPos, pos)
            } else if (piece != null && piece.isWhite == isWhiteTurn) {
                val targets = computeValidMovesForPiece(pos, piece, state.chessBoard)
                _uiState.value = state.copy(selectedSquare = pos, validMoves = targets)
            } else {
                _uiState.value = state.copy(selectedSquare = null, validMoves = emptyList())
            }
        }
    }

    private fun executeChessMove(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        val state = _uiState.value
        val newBoard = state.chessBoard.toMutableMap()
        val movingPiece = newBoard.remove(from) ?: return
        val targetPiece = newBoard[to]

        // Handle Pawn Promotion
        val isPromotion = movingPiece.type == "P" && (to.first == 0 || to.first == 7)
        val finalPiece = if (isPromotion) ChessPiece("Q", movingPiece.isWhite) else movingPiece

        newBoard[to] = finalPiece

        val whiteCap = state.whiteCaptured.toMutableList()
        val blackCap = state.blackCaptured.toMutableList()
        if (targetPiece != null) {
            if (targetPiece.isWhite) whiteCap.add(targetPiece.type) else blackCap.add(targetPiece.type)
        }

        val moveNotation = "${algebraicSquare(from)}${algebraicSquare(to)}"
        val nextTurn = if (state.chessTurn == "w") "b" else "w"
        val updatedLog = state.chessMoveLog + "User: $moveNotation"

        _uiState.value = state.copy(
            chessBoard = newBoard,
            selectedSquare = null,
            validMoves = emptyList(),
            chessTurn = nextTurn,
            chessMoveLog = updatedLog,
            whiteCaptured = whiteCap,
            blackCaptured = blackCap,
            chessMoveHistoryStack = state.chessMoveHistoryStack + listOf(state.chessBoard),
            isChessAiThinking = true
        )

        viewModelScope.launch {
            delay(600) // AI thinking delay
            triggerAiChessMove(newBoard, nextTurn)
        }
    }

    private fun triggerAiChessMove(board: Map<Pair<Int, Int>, ChessPiece>, aiTurn: String) {
        val state = _uiState.value
        val isAiWhite = (aiTurn == "w")
        val aiPieces = board.filter { it.value.isWhite == isAiWhite }
        if (aiPieces.isEmpty()) return

        val allAiMoves = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
        for ((fromPos, p) in aiPieces) {
            val valid = computeValidMovesForPiece(fromPos, p, board)
            for (toPos in valid) {
                allAiMoves.add(Pair(fromPos, toPos))
            }
        }

        if (allAiMoves.isEmpty()) {
            _uiState.value = state.copy(chessStatus = "STALEMATE", chessWinner = "draw", isChessAiThinking = false)
            return
        }

        val chosenMove = when (state.chessDifficulty) {
            "Easy" -> allAiMoves.random()
            "Hard" -> {
                // Prefer captures
                allAiMoves.firstOrNull { board.containsKey(it.second) } ?: allAiMoves.random()
            }
            else -> allAiMoves.random()
        }

        val (from, to) = chosenMove
        val newBoard = board.toMutableMap()
        val movingPiece = newBoard.remove(from) ?: return
        val targetPiece = newBoard[to]

        val isPromotion = movingPiece.type == "P" && (to.first == 0 || to.first == 7)
        newBoard[to] = if (isPromotion) ChessPiece("Q", movingPiece.isWhite) else movingPiece

        val whiteCap = state.whiteCaptured.toMutableList()
        val blackCap = state.blackCaptured.toMutableList()
        if (targetPiece != null) {
            if (targetPiece.isWhite) whiteCap.add(targetPiece.type) else blackCap.add(targetPiece.type)
        }

        val moveNotation = "${algebraicSquare(from)}${algebraicSquare(to)}"
        val updatedLog = state.chessMoveLog + "Game Mind AI: $moveNotation"

        _uiState.value = _uiState.value.copy(
            chessBoard = newBoard,
            chessTurn = if (aiTurn == "w") "b" else "w",
            chessMoveLog = updatedLog,
            whiteCaptured = whiteCap,
            blackCaptured = blackCap,
            isChessAiThinking = false
        )
    }

    private fun computeValidMovesForPiece(pos: Pair<Int, Int>, piece: ChessPiece, board: Map<Pair<Int, Int>, ChessPiece>): List<Pair<Int, Int>> {
        val (r, c) = pos
        val valid = mutableListOf<Pair<Int, Int>>()
        val isW = piece.isWhite

        when (piece.type) {
            "P" -> {
                val dir = if (isW) -1 else 1
                val f1 = Pair(r + dir, c)
                if (f1.first in 0..7 && !board.containsKey(f1)) {
                    valid.add(f1)
                    val startR = if (isW) 6 else 1
                    val f2 = Pair(r + 2 * dir, c)
                    if (r == startR && f2.first in 0..7 && !board.containsKey(f2)) {
                        valid.add(f2)
                    }
                }
                for (dc in listOf(-1, 1)) {
                    val capPos = Pair(r + dir, c + dc)
                    if (capPos.first in 0..7 && capPos.second in 0..7) {
                        val target = board[capPos]
                        if (target != null && target.isWhite != isW) {
                            valid.add(capPos)
                        }
                    }
                }
            }
            "N" -> {
                val jumps = listOf(Pair(-2,-1), Pair(-2,1), Pair(-1,-2), Pair(-1,2), Pair(1,-2), Pair(1,2), Pair(2,-1), Pair(2,1))
                for (j in jumps) {
                    val target = Pair(r + j.first, c + j.second)
                    if (target.first in 0..7 && target.second in 0..7) {
                        val occup = board[target]
                        if (occup == null || occup.isWhite != isW) valid.add(target)
                    }
                }
            }
            "B", "R", "Q" -> {
                val dirs = mutableListOf<Pair<Int, Int>>()
                if (piece.type in listOf("B", "Q")) dirs.addAll(listOf(Pair(-1,-1), Pair(-1,1), Pair(1,-1), Pair(1,1)))
                if (piece.type in listOf("R", "Q")) dirs.addAll(listOf(Pair(-1,0), Pair(1,0), Pair(0,-1), Pair(0,1)))

                for (d in dirs) {
                    var currR = r + d.first
                    var currC = c + d.second
                    while (currR in 0..7 && currC in 0..7) {
                        val t = Pair(currR, currC)
                        val occup = board[t]
                        if (occup == null) {
                            valid.add(t)
                        } else {
                            if (occup.isWhite != isW) valid.add(t)
                            break
                        }
                        currR += d.first
                        currC += d.second
                    }
                }
            }
            "K" -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val target = Pair(r + dr, c + dc)
                        if (target.first in 0..7 && target.second in 0..7) {
                            val occup = board[target]
                            if (occup == null || occup.isWhite != isW) valid.add(target)
                        }
                    }
                }
            }
        }
        return valid
    }

    fun undoChessMove() {
        val state = _uiState.value
        if (state.chessMoveHistoryStack.isNotEmpty()) {
            val lastBoard = state.chessMoveHistoryStack.last()
            _uiState.value = state.copy(
                chessBoard = lastBoard,
                selectedSquare = null,
                validMoves = emptyList(),
                chessMoveHistoryStack = state.chessMoveHistoryStack.dropLast(1),
                chessMoveLog = state.chessMoveLog + "User: Undo Move"
            )
        }
    }

    fun resetChess() {
        _uiState.value = _uiState.value.copy(
            chessBoard = defaultChessBoard(),
            chessTurn = "w",
            selectedSquare = null,
            validMoves = emptyList(),
            chessMoveLog = listOf("Match reset vs Game Mind AI"),
            whiteCaptured = emptyList(),
            blackCaptured = emptyList(),
            chessStatus = "IN_PROGRESS",
            chessWinner = null,
            isChessAiThinking = false,
            chessMoveHistoryStack = emptyList()
        )
    }

    fun setChessDifficulty(diff: String) {
        _uiState.value = _uiState.value.copy(chessDifficulty = diff)
    }

    // --- Maze Controls & Pathfinding ---
    fun generateMaze(size: Int = 11) {
        mazeTimerJob?.cancel()
        val data = GameEngineHelper.generateMaze(size, size)
        _uiState.value = _uiState.value.copy(
            mazeSize = size,
            mazeGrid = data.grid,
            startPos = data.start,
            endPos = data.end,
            playerPos = data.start,
            solutionPath = emptyList(),
            isMazeSolved = false,
            mazeStepCount = 0,
            mazeTimerSeconds = 0,
            isRaceMode = false,
            aiRacePos = data.start
        )
        startMazeTimer()
    }

    private fun startMazeTimer() {
        mazeTimerJob?.cancel()
        mazeTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isMazeSolved) {
                    _uiState.value = _uiState.value.copy(mazeTimerSeconds = _uiState.value.mazeTimerSeconds + 1)
                }
            }
        }
    }

    fun movePlayerMaze(dr: Int, dc: Int) {
        val state = _uiState.value
        if (state.isMazeSolved) return

        val nr = state.playerPos.first + dr
        val nc = state.playerPos.second + dc

        if (nr in state.mazeGrid.indices && nc in state.mazeGrid[0].indices && state.mazeGrid[nr][nc] == 0) {
            val newPos = Pair(nr, nc)
            val isWon = (newPos == state.endPos)
            val newSteps = state.mazeStepCount + 1

            val newRecord = if (isWon) {
                GameHistoryRecord(gameType = "AI Maze", result = "Won", score = 800 - newSteps * 10, totalMoves = newSteps)
            } else null

            _uiState.value = state.copy(
                playerPos = newPos,
                mazeStepCount = newSteps,
                isMazeSolved = isWon,
                gameHistory = if (newRecord != null) listOf(newRecord) + state.gameHistory else state.gameHistory
            )

            if (isWon) {
                viewModelScope.launch {
                    repository.saveHistory("GAME_MAZE", "Maze Solved!", "Completed maze in $newSteps steps & ${state.mazeTimerSeconds}s")
                }
            }
        }
    }

    fun solveMazeAStar() {
        val state = _uiState.value
        val path = GameEngineHelper.solveMazeAStar(state.mazeGrid, state.startPos, state.endPos)
        _uiState.value = state.copy(
            solutionPath = path,
            isMazeSolved = path.isNotEmpty()
        )
    }

    fun startMazeRace() {
        val state = _uiState.value
        val path = GameEngineHelper.solveMazeAStar(state.mazeGrid, state.startPos, state.endPos)
        _uiState.value = state.copy(isRaceMode = true, solutionPath = path)

        viewModelScope.launch {
            for (step in path) {
                delay(300)
                if (!_uiState.value.isRaceMode) break
                _uiState.value = _uiState.value.copy(aiRacePos = step)
            }
        }
    }

    // --- AI Coach LLM Advice ---
    fun askAiCoach(gameType: String) {
        _uiState.value = _uiState.value.copy(isAiCoachLoading = true, aiCoachDialogTitle = "$gameType AI Coach Advice")

        viewModelScope.launch {
            val contextDesc = when (gameType) {
                "Chess" -> "Current Chess FEN: ${_uiState.value.chessFen}, Moves: ${_uiState.value.chessMoveLog.takeLast(4)}"
                "Tic-Tac-Toe" -> "Board: ${_uiState.value.ticTacToeBoard}, Winner: ${_uiState.value.ticTacToeWinner}"
                else -> "Maze Size: ${_uiState.value.mazeSize}x${_uiState.value.mazeSize}, Steps: ${_uiState.value.mazeStepCount}"
            }
            val prompt = "Give me 3 brief grandmaster tips for $gameType based on this state: $contextDesc"
            val res = repository.sendChatMessage(prompt)
            res.onSuccess { reply ->
                _uiState.value = _uiState.value.copy(isAiCoachLoading = false, aiCoachMessage = reply.reply)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isAiCoachLoading = false,
                    aiCoachMessage = "Focus on controlling the center squares, developing knights and bishops early, and maintaining king safety!"
                )
            }
        }
    }

    fun dismissAiCoachDialog() {
        _uiState.value = _uiState.value.copy(aiCoachDialogTitle = null, aiCoachMessage = null, isAiCoachLoading = false)
    }

    private fun algebraicSquare(p: Pair<Int, Int>): String {
        return "${( 'a' + p.second)}${8 - p.first}"
    }

    override fun onCleared() {
        super.onCleared()
        mazeTimerJob?.cancel()
    }
}
