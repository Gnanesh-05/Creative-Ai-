package com.example.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.GamesViewModel

@Composable
fun MazeGameScreen(
    gamesViewModel: GamesViewModel
) {
    val uiState by gamesViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AI Maze Pathfinder", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("A* Heuristic Search & Pathfinding Visualizer", fontSize = 11.sp, color = Color(0xFF4FD8EB))
            }
            Row {
                IconButton(onClick = { gamesViewModel.askAiCoach("AI Maze") }, modifier = Modifier.testTag("maze_ai_coach_button")) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Explain Algorithm", tint = Color(0xFF4FD8EB))
                }
                IconButton(onClick = { gamesViewModel.generateMaze(uiState.mazeSize) }, modifier = Modifier.testTag("maze_regenerate_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = "New Maze", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Size Selector & Counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(7, 11, 15).forEach { sz ->
                    FilterChip(
                        selected = uiState.mazeSize == sz,
                        onClick = { gamesViewModel.generateMaze(sz) },
                        label = { Text("${sz}x${sz}", fontSize = 10.sp) },
                        modifier = Modifier.testTag("maze_size_$sz")
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Steps: ${uiState.mazeStepCount}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Time: ${uiState.mazeTimerSeconds}s", fontSize = 12.sp, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Maze Grid representation
        val grid = uiState.mazeGrid
        if (grid.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(2.dp, Color(0xFF4FD8EB), RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F0C20))
                    .padding(4.dp)
            ) {
                Column {
                    grid.forEachIndexed { r, row ->
                        Row(modifier = Modifier.weight(1f)) {
                            row.forEachIndexed { c, cell ->
                                val pos = Pair(r, c)
                                val isPlayer = pos == uiState.playerPos
                                val isStart = pos == uiState.startPos
                                val isEnd = pos == uiState.endPos
                                val isSolution = pos in uiState.solutionPath
                                val isAiRace = uiState.isRaceMode && pos == uiState.aiRacePos

                                val cellColor = when {
                                    isPlayer -> Color(0xFF4FD8EB) // User Player Cyan
                                    isAiRace -> Color(0xFFFFB4A2) // AI Race Pos
                                    isEnd -> Color(0xFFF5B8FF) // Goal Magenta
                                    isSolution -> Color(0xFFD0BCFF).copy(alpha = 0.8f) // A* Solution Path
                                    isStart -> Color(0xFF4FD8EB).copy(alpha = 0.4f)
                                    cell == 1 -> Color(0xFF2A1B54) // Wall
                                    else -> Color(0xFF0F0C20) // Open Path
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(cellColor, shape = RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlayer) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // D-Pad Directional Controls for Manual Play
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { gamesViewModel.movePlayerMaze(-1, 0) }, modifier = Modifier.testTag("maze_dpad_up")) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { gamesViewModel.movePlayerMaze(0, -1) }, modifier = Modifier.testTag("maze_dpad_left")) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Box(modifier = Modifier.size(24.dp))
                IconButton(onClick = { gamesViewModel.movePlayerMaze(0, 1) }, modifier = Modifier.testTag("maze_dpad_right")) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
            IconButton(onClick = { gamesViewModel.movePlayerMaze(1, 0) }, modifier = Modifier.testTag("maze_dpad_down")) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Solver Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { gamesViewModel.solveMazeAStar() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD8EB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("maze_solve_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF0F0C20))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run A* Solver", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = { gamesViewModel.startMazeRace() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("maze_race_button")
            ) {
                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF0F0C20))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Race vs AI", color = Color(0xFF0F0C20), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (uiState.isMazeSolved) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Maze Completed in ${uiState.mazeStepCount} steps & ${uiState.mazeTimerSeconds}s!",
                color = Color(0xFF4FD8EB),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
