package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.components.GlassCard
import com.example.frontend.theme.DarkSurface
import com.example.frontend.theme.GlassBorder
import com.example.frontend.theme.NexusCyan
import com.example.frontend.theme.NexusIndigo
import com.example.frontend.theme.NexusPurple

@Composable
fun WorkspaceScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val memoryFacts by viewModel.memoryFacts.collectAsState()

    var showAddModal by remember { mutableStateOf(false) }
    var newFactText by remember { mutableStateOf("") }
    var newFactCategory by remember { mutableStateOf("Preference") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NexusCyan
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexusIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = NexusCyan)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Workspace & Semantic Memory",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Long-Term RAG recall, projects & user preferences",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }

                    IconButton(
                        onClick = { showAddModal = !showAddModal },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NexusCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memory", tint = Color.Black)
                    }
                }
            }
        }

        // Add Memory Modal Form
        item {
            AnimatedVisibility(visible = showAddModal) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NexusCyan
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Record New Semantic Fact",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newFactText,
                            onValueChange = { newFactText = it },
                            placeholder = { Text("e.g. Always use Jetpack Compose for UI code", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF131722),
                                unfocusedContainerColor = Color(0xFF131722),
                                focusedBorderColor = NexusCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val categories = listOf("Preference", "Project", "Personal")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                categories.forEach { cat ->
                                    val isSelected = cat == newFactCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) NexusCyan else Color(0xFF1F2937))
                                            .clickable { newFactCategory = cat }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = cat, style = MaterialTheme.typography.labelSmall.copy(color = if (isSelected) Color.Black else Color.White))
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (newFactText.isNotBlank()) {
                                        viewModel.addMemoryFact(newFactCategory, newFactText, true)
                                        newFactText = ""
                                        showAddModal = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NexusCyan)
                            ) {
                                Text("Save Fact", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Memory List Section
        item {
            Text(
                text = "Semantic Memory Database (${memoryFacts.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
        }

        items(memoryFacts) { fact ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (fact.isPinned) NexusCyan else GlassBorder
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.togglePinFact(fact.id, fact.isPinned) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (fact.isPinned) Icons.Default.PushPin else Icons.Default.BookmarkBorder,
                            contentDescription = "Pin",
                            tint = if (fact.isPinned) NexusCyan else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "[${fact.category}] ${fact.fact}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 13.sp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteMemoryFact(fact.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }

        // Pinned Projects Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pinned Workspace Projects",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
        }

        item {
            val projects = listOf("Nexus AI OS Android Kernel", "Diffusion Prompts Vault", "Lo-fi Music Synthesis Engine")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { proj ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = NexusPurple)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = proj, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }
        }
    }
}
