package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as CanvasBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.components.AudioWaveVisualizer
import com.example.frontend.components.GlassCard
import com.example.frontend.theme.IridescentGradient
import com.example.frontend.theme.LightCanvasEnd
import com.example.frontend.theme.LightCanvasMid
import com.example.frontend.theme.LightCanvasStart
import com.example.frontend.theme.LightGlassBorder
import com.example.frontend.theme.LightGlassSurface
import com.example.frontend.theme.PastelCyan
import com.example.frontend.theme.PastelMagenta
import com.example.frontend.theme.PastelViolet
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight
import com.example.backend.util.MediaSharingUtils

@Composable
fun MusicStudioScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompt by viewModel.musicPrompt.collectAsState()
    val selectedGenre by viewModel.musicGenre.collectAsState()
    val selectedMood by viewModel.musicMood.collectAsState()
    val tempo by viewModel.musicTempo.collectAsState()
    val selectedInstruments by viewModel.selectedInstruments.collectAsState()
    val isPlaying by viewModel.isMusicPlaying.collectAsState()
    val isGenerating by viewModel.isGeneratingMusic.collectAsState()
    val stepText by viewModel.generationStepText.collectAsState()
    val assets by viewModel.studioAssets.collectAsState()

    val musicAssets = assets.filter { it.type == "MUSIC" }
    val latestAsset = musicAssets.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = CanvasBrush.verticalGradient(
                    colors = listOf(
                        LightCanvasStart,
                        LightCanvasMid,
                        LightCanvasEnd
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Real-Time Audio Synthesizer Player
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LightGlassSurface,
                borderColor = if (isPlaying) PastelViolet else LightGlassBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (prompt.isBlank()) "$selectedGenre Track" else prompt,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight),
                                maxLines = 1
                            )
                            Text(
                                text = "Genre: $selectedGenre • Tempo: ${tempo.toInt()} BPM • Real Audio Engine",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleMusicPlayback(context, latestAsset?.assetUri) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PastelViolet)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    AudioWaveVisualizer(isPlaying = isPlaying)
                }
            }
        }

        // Song Prompt Input & Quick Sample Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Song & Lyrics Concept Prompt (Fast 2.0s Engine)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextPrimaryLight,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.setMusicPrompt(it) },
                    placeholder = {
                        Text(text = "e.g. A relaxing lofi track about studying on a rainy afternoon...", color = TextMutedLight)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = TextPrimaryLight,
                        unfocusedTextColor = TextPrimaryLight
                    ),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Quick Song Prompts (1-Tap Real Generation)",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val samplePrompts = listOf(
                    "🌧️ Rainy afternoon lofi beat about coffee",
                    "🏎️ 80s Synthwave anthem for late night drive",
                    "🎸 Acoustic indie pop ballad about summer love",
                    "🎹 Cinematic piano soundtrack for epic scenes"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(samplePrompts) { chipPrompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable { viewModel.generateMusicTrack(context, chipPrompt) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipPrompt,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimaryLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Genre Picker
        item {
            Column {
                Text(
                    text = "Genre Style",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextPrimaryLight, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val genres = listOf("Lo-fi", "Synthwave", "Orchestral", "Chill Ambient", "Cyberpunk", "Pop Ballad")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genres) { g ->
                        val isSelected = g == selectedGenre
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PastelViolet else Color.White.copy(alpha = 0.8f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setMusicGenre(g) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = g,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) Color.White else TextSecondaryLight,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        // Tempo Slider
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LightGlassSurface,
                borderColor = LightGlassBorder
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tempo Control", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight))
                        Text("${tempo.toInt()} BPM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = PastelViolet))
                    }
                    Slider(
                        value = tempo,
                        onValueChange = { viewModel.setMusicTempo(it) },
                        valueRange = 60f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = PastelViolet,
                            activeTrackColor = PastelViolet,
                            inactiveTrackColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }

        // Generate Song & Lyrics Button
        item {
            Button(
                onClick = { viewModel.generateMusicTrack(context) },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PastelViolet,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stepText, color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Song & Synthesize Music (2.0s)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Music & Lyrics Output History
        item {
            Text(
                text = "Composed Songs & Lyrics (${musicAssets.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight)
            )
        }

        if (musicAssets.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.LibraryMusic, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No songs composed yet. Tap a quick prompt above!", style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedLight))
                    }
                }
            }
        } else {
            items(musicAssets) { asset ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = PastelViolet)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = asset.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                                )
                            }

                            // Real Audio Play Button on Card
                            IconButton(
                                onClick = { viewModel.toggleMusicPlayback(context, asset.assetUri) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PastelViolet)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Track",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Concept: ${asset.prompt}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Lyrics Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = asset.paramsJson,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = TextPrimaryLight,
                                    lineHeight = 18.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Download & Share Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Download Audio & Lyrics
                            Button(
                                onClick = {
                                    MediaSharingUtils.downloadSongAndLyrics(context, asset.title, asset.paramsJson, asset.assetUri)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelViolet),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Share via WhatsApp
                            Button(
                                onClick = {
                                    MediaSharingUtils.shareMusicViaWhatsApp(context, asset.title, asset.paramsJson, asset.assetUri)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
