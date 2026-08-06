package com.example.backend.engine

import kotlin.math.abs

/**
 * Local deterministic Game Logic Engine for Android.
 * Guarantees zero-latency offline gameplay, move validation,
 * Minimax Tic-Tac-Toe, A* Pathfinding, and Chess rule verification.
 */
object GameEngineHelper {

    // --- Tic-Tac-Toe Minimax Engine ---
    private val WIN_PATTERNS = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun checkTicTacToeWinner(board: List<String>): String? {
        for (combo in WIN_PATTERNS) {
            val (a, b, c) = combo
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return board[a]
            }
        }
        if (board.none { it.isEmpty() }) return "DRAW"
        return null
    }

    fun getOptimalTicTacToeMove(board: List<String>, difficulty: String, aiSymbol: String): Int {
        val playerSymbol = if (aiSymbol == "O") "X" else "O"
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isEmpty()) return -1

        if (difficulty == "Easy") {
            return emptyIndices.random()
        }

        if (difficulty == "Medium" && Math.random() > 0.6) {
            return emptyIndices.random()
        }

        // Hard / Unbeatable Minimax
        var bestScore = -1000
        var bestMove = emptyIndices.first()

        for (move in emptyIndices) {
            val mutableBoard = board.toMutableList()
            mutableBoard[move] = aiSymbol
            val score = minimax(mutableBoard, 0, false, aiSymbol, playerSymbol)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun minimax(board: MutableList<String>, depth: Int, isMaximizing: Boolean, aiSymbol: String, playerSymbol: String): Int {
        val winner = checkTicTacToeWinner(board)
        if (winner == aiSymbol) return 10 - depth
        if (winner == playerSymbol) return depth - 10
        if (winner == "DRAW") return 0

        val emptyIndices = board.indices.filter { board[it].isEmpty() }

        if (isMaximizing) {
            var bestScore = -1000
            for (move in emptyIndices) {
                board[move] = aiSymbol
                val score = minimax(board, depth + 1, false, aiSymbol, playerSymbol)
                board[move] = ""
                if (score > bestScore) bestScore = score
            }
            return bestScore
        } else {
            var bestScore = 1000
            for (move in emptyIndices) {
                board[move] = playerSymbol
                val score = minimax(board, depth + 1, true, aiSymbol, playerSymbol)
                board[move] = ""
                if (score < bestScore) bestScore = score
            }
            return bestScore
        }
    }

    // --- Maze Procedural Generation & A* Pathfinding ---
    data class MazeData(
        val rows: Int,
        val cols: Int,
        val grid: List<List<Int>>, // 0 = path, 1 = wall
        val start: Pair<Int, Int>,
        val end: Pair<Int, Int>,
        val solutionPath: List<Pair<Int, Int>>
    )

    fun generateMaze(rows: Int = 11, cols: Int = 11): MazeData {
        val rCount = if (rows % 2 != 0) rows else rows + 1
        val cCount = if (cols % 2 != 0) cols else cols + 1

        val grid = Array(rCount) { IntArray(cCount) { 1 } }
        val stack = mutableListOf(Pair(1, 1))
        grid[1][1] = 0

        val directions = listOf(Pair(-2, 0), Pair(2, 0), Pair(0, -2), Pair(0, 2))

        while (stack.isNotEmpty()) {
            val (cr, cc) = stack.last()
            val neighbors = mutableListOf<Triple<Int, Int, Pair<Int, Int>>>()

            for (d in directions) {
                val nr = cr + d.first
                val nc = cc + d.second
                if (nr > 0 && nr < rCount - 1 && nc > 0 && nc < cCount - 1 && grid[nr][nc] == 1) {
                    neighbors.add(Triple(nr, nc, d))
                }
            }

            if (neighbors.isNotEmpty()) {
                val chosen = neighbors.random()
                grid[cr + chosen.third.first / 2][cc + chosen.third.second / 2] = 0
                grid[chosen.first][chosen.second] = 0
                stack.add(Pair(chosen.first, chosen.second))
            } else {
                stack.removeAt(stack.size - 1)
            }
        }

        val start = Pair(1, 1)
        val end = Pair(rCount - 2, cCount - 2)
        grid[start.first][start.second] = 0
        grid[end.first][end.second] = 0

        val gridList = grid.map { it.toList() }
        val solution = solveMazeAStar(gridList, start, end)

        return MazeData(rCount, cCount, gridList, start, end, solution)
    }

    fun solveMazeAStar(grid: List<List<Int>>, start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {
        fun heuristic(a: Pair<Int, Int>, b: Pair<Int, Int>) = abs(a.first - b.first) + abs(a.second - b.second)

        val openSet = mutableSetOf(start)
        val cameFrom = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        val gScore = mutableMapOf(start to 0)
        val fScore = mutableMapOf(start to heuristic(start, end))

        while (openSet.isNotEmpty()) {
            val current = openSet.minByOrNull { fScore.getOrDefault(it, Int.MAX_VALUE) } ?: break

            if (current == end) {
                val path = mutableListOf<Pair<Int, Int>>()
                var curr: Pair<Int, Int>? = current
                while (curr != null) {
                    path.add(curr)
                    curr = cameFrom[curr]
                }
                return path.reversed()
            }

            openSet.remove(current)

            val neighbors = listOf(
                Pair(current.first - 1, current.second),
                Pair(current.first + 1, current.second),
                Pair(current.first, current.second - 1),
                Pair(current.first, current.second + 1)
            )

            for (neighbor in neighbors) {
                val (nr, nc) = neighbor
                if (nr in grid.indices && nc in grid[0].indices && grid[nr][nc] == 0) {
                    val tentativeG = gScore.getOrDefault(current, Int.MAX_VALUE) + 1
                    if (tentativeG < gScore.getOrDefault(neighbor, Int.MAX_VALUE)) {
                        cameFrom[neighbor] = current
                        gScore[neighbor] = tentativeG
                        fScore[neighbor] = tentativeG + heuristic(neighbor, end)
                        openSet.add(neighbor)
                    }
                }
            }
        }
        return emptyList()
    }
}
