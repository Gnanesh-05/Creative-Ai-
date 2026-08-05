package com.example.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AiModuleCardData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val accentColor: Color
)

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val modules = listOf(
        AiModuleCardData("Chat AI", "Conversational Gemini LLM engine", Icons.Default.Chat, "chat", Color(0xFF6750A4)),
        AiModuleCardData("Image Studio", "Photorealistic AI image generator", Icons.Default.Brush, "image_generator", Color(0xFF006874)),
        AiModuleCardData("Music Composer", "AI synthesized ambient compositions", Icons.Default.MusicNote, "music_composer", Color(0xFF984061)),
        AiModuleCardData("Game Mind AI", "Chess, Tic-Tac-Toe & AI Maze Solver", Icons.Default.SportsEsports, "game_mind", Color(0xFF8C4E2A)),
        AiModuleCardData("Unified History", "Cross-module interaction logs", Icons.Default.History, "unified_history", Color(0xFF00639A)),
        AiModuleCardData("Profile & Tier", "Usage statistics & account level", Icons.Default.Person, "profile", Color(0xFF386A20))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8FF))
            .padding(16.dp)
            .testTag("home_screen_container")
    ) {
        // Hero Header Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Creative AI",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Creative AI Hub",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome to your multi-modal intelligence workspace. Select a module below to begin creating.",
                    fontSize = 14.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AI Modules & Engines",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B1E),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(modules) { item ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clickable { onNavigate(item.route) }
                        .testTag("home_module_card_${item.route}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B1E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = Color(0xFF49454F),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
