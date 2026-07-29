package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MusicResultDomain
import com.example.ui.viewmodels.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MusicComposerScreen(
    musicViewModel: MusicViewModel
) {
    val uiState by musicViewModel.uiState.collectAsState()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1B0D2A), Color(0xFF0D0616), Color(0xFF140B1F))
    )

    val genres = listOf(
        "Cinematic", "Lo-Fi Beats", "Electronic", "Ambient",
        "Orchestral", "Jazz", "Synthwave", "Acoustic", "Rock"
    )

    val moods = listOf(
        "Relaxing", "Energetic", "Emotional", "Dark",
        "Uplifting", "Melancholic", "Epic"
    )

    val keySignatures = listOf(
        "C Major", "A Minor", "D Minor", "G Major", "F Major", "E Minor", "Pentatonic"
    )

    val durations = listOf(15, 30, 60, 120)

    val instrumentsList = listOf(
        "Grand Piano", "Electric Guitar", "Synth Arp",
        "Strings Ensemble", "Flute", "Drums & Bass", "Acoustic Guitar"
    )

    val energyLevels = listOf("Low", "Medium", "High", "Intense")

    var isControlsExpanded by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = Color(0xFF160A22).copy(alpha = 0.95f),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFF5B8FF), Color(0xFF984061))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "AI Music Composer",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Synthesize original compositions & scores",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF5B8FF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector
                    SecondaryTabRow(
                        selectedTabIndex = uiState.activeTab,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFF5B8FF)
                    ) {
                        Tab(
                            selected = uiState.activeTab == 0,
                            onClick = { musicViewModel.setActiveTab(0) },
                            text = { Text("Studio Composer", fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            selectedContentColor = Color(0xFFF5B8FF),
                            unselectedContentColor = Color.White.copy(alpha = 0.6f)
                        )
                        Tab(
                            selected = uiState.activeTab == 1,
                            onClick = { musicViewModel.setActiveTab(1) },
                            text = { Text("History (${uiState.history.size})", fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            selectedContentColor = Color(0xFFF5B8FF),
                            unselectedContentColor = Color.White.copy(alpha = 0.6f)
                        )
                        Tab(
                            selected = uiState.activeTab == 2,
                            onClick = { musicViewModel.setActiveTab(2) },
                            text = { Text("Saved (${uiState.savedTracks.size})", fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            selectedContentColor = Color(0xFFF5B8FF),
                            unselectedContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
                .testTag("music_composer_screen_container")
        ) {
            when (uiState.activeTab) {
                0 -> {
                    // Studio Composer Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        // Prompt Input Card
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF241434)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5B8FF).copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Describe What You Want To Hear",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        // Gemini Prompt Enhancer Button
                                        Surface(
                                            onClick = { musicViewModel.enhancePrompt() },
                                            enabled = !uiState.isEnhancing && uiState.prompt.isNotBlank(),
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFF984061).copy(alpha = 0.3f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5B8FF).copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                if (uiState.isEnhancing) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        color = Color(0xFFF5B8FF),
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = Color(0xFFF5B8FF),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("AI Enhance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF5B8FF))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = uiState.prompt,
                                        onValueChange = { musicViewModel.updatePrompt(it) },
                                        placeholder = { Text("e.g. An emotional cinematic piano composition for a movie climax beside a rain-soaked window...") },
                                        minLines = 3,
                                        maxLines = 5,
                                        trailingIcon = {
                                            if (uiState.prompt.isNotEmpty()) {
                                                IconButton(onClick = { musicViewModel.updatePrompt("") }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFF5B8FF),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("music_prompt_input")
                                    )

                                    // Display Enhanced Prompt if active
                                    AnimatedVisibility(visible = uiState.enhancedPrompt != null) {
                                        uiState.enhancedPrompt?.let { enhanced ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF321A42)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5B8FF).copy(alpha = 0.5f))
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF5B8FF), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Gemini Enhanced Prompt Specification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF5B8FF))
                                                        }
                                                        IconButton(
                                                            onClick = { musicViewModel.clearEnhancedPrompt() },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(enhanced, fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f), lineHeight = 16.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Advanced Controls Accordion Header
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF241434)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isControlsExpanded = !isControlsExpanded }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFF5B8FF), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Composition Controls", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                "${uiState.genre} • ${uiState.mood} • ${uiState.tempoBpm} BPM • ${uiState.keySignature}",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                    Icon(
                                        if (isControlsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Advanced Composition Controls Section
                        if (isControlsExpanded) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0F2E)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Genre Selector
                                        Column {
                                            Text("Genre", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(genres) { g ->
                                                    FilterChip(
                                                        selected = uiState.genre == g,
                                                        onClick = { musicViewModel.updateGenre(g) },
                                                        label = { Text(g, fontSize = 12.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFFF5B8FF),
                                                            selectedLabelColor = Color(0xFF0F0C20),
                                                            containerColor = Color(0xFF2D183B),
                                                            labelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Mood Selector
                                        Column {
                                            Text("Mood", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(moods) { m ->
                                                    FilterChip(
                                                        selected = uiState.mood == m,
                                                        onClick = { musicViewModel.updateMood(m) },
                                                        label = { Text(m, fontSize = 12.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFF984061),
                                                            selectedLabelColor = Color.White,
                                                            containerColor = Color(0xFF2D183B),
                                                            labelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Tempo / BPM Slider
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Tempo (BPM)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("${uiState.tempoBpm} BPM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF5B8FF))
                                            }
                                            Slider(
                                                value = uiState.tempoBpm.toFloat(),
                                                onValueChange = { musicViewModel.updateTempo(it.toInt()) },
                                                valueRange = 60f..180f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Color(0xFFF5B8FF),
                                                    activeTrackColor = Color(0xFFF5B8FF),
                                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                                )
                                            )
                                        }

                                        // Key Signature Selector
                                        Column {
                                            Text("Key Signature", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(keySignatures) { k ->
                                                    FilterChip(
                                                        selected = uiState.keySignature == k,
                                                        onClick = { musicViewModel.updateKeySignature(k) },
                                                        label = { Text(k, fontSize = 12.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFFF5B8FF),
                                                            selectedLabelColor = Color(0xFF0F0C20),
                                                            containerColor = Color(0xFF2D183B),
                                                            labelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Duration Selector
                                        Column {
                                            Text("Track Duration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                durations.forEach { d ->
                                                    FilterChip(
                                                        selected = uiState.durationSeconds == d,
                                                        onClick = { musicViewModel.updateDuration(d) },
                                                        label = { Text("${d}s", fontSize = 12.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFFF5B8FF),
                                                            selectedLabelColor = Color(0xFF0F0C20),
                                                            containerColor = Color(0xFF2D183B),
                                                            labelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Primary Instrument Selector
                                        Column {
                                            Text("Primary Instrument", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(instrumentsList) { inst ->
                                                    FilterChip(
                                                        selected = uiState.instruments.contains(inst),
                                                        onClick = { musicViewModel.updateInstruments(inst) },
                                                        label = { Text(inst, fontSize = 12.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFF984061),
                                                            selectedLabelColor = Color.White,
                                                            containerColor = Color(0xFF2D183B),
                                                            labelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Energy Level
                                        Column {
                                            Text("Energy Level", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                energyLevels.forEach { e ->
                                                    FilterChip(
                                                        selected = uiState.energyLevel == e,
                                                        onClick = { musicViewModel.updateEnergyLevel(e) },
                                                        label = { Text(e, fontSize = 12.sp) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFFF5B8FF),
                                                            selectedLabelColor = Color(0xFF0F0C20),
                                                            containerColor = Color(0xFF2D183B),
                                                            labelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Instrumental vs Vocals Toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text("Instrumental Composition", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(
                                                    if (uiState.isInstrumental) "Pure instrumental stems" else "Vocal accompaniment & lyrics",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                            }
                                            Switch(
                                                checked = uiState.isInstrumental,
                                                onCheckedChange = { musicViewModel.updateIsInstrumental(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF0F0C20),
                                                    checkedTrackColor = Color(0xFFF5B8FF)
                                                )
                                            )
                                        }

                                        // Optional Lyrics Input
                                        if (!uiState.isInstrumental) {
                                            OutlinedTextField(
                                                value = uiState.lyrics,
                                                onValueChange = { musicViewModel.updateLyrics(it) },
                                                label = { Text("Song Lyrics / Vocal Script") },
                                                placeholder = { Text("Enter lyrics or let AI compose vocal melody...") },
                                                minLines = 2,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFF5B8FF),
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Generate Music Button Card
                        item {
                            Column {
                                Button(
                                    onClick = { musicViewModel.generateMusic() },
                                    enabled = !uiState.isGenerating && uiState.prompt.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF5B8FF),
                                        disabledContainerColor = Color(0xFFF5B8FF).copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("music_compose_button")
                                ) {
                                    if (uiState.isGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color(0xFF0F0C20),
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Synthesizing Track (${uiState.generationProgress}%)...",
                                            color = Color(0xFF0F0C20),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF0F0C20))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Generate AI Composition",
                                            color = Color(0xFF0F0C20),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (uiState.isGenerating) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { uiState.generationProgress / 100f },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFFF5B8FF),
                                            trackColor = Color.White.copy(alpha = 0.2f)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        TextButton(onClick = { musicViewModel.cancelActiveJob() }) {
                                            Text("Cancel", color = Color(0xFFF5B8FF), fontSize = 12.sp)
                                        }
                                    }
                                }

                                uiState.errorMessage?.let { err ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }

                        // Active Track Audio Player Card
                        uiState.currentTrack?.let { track ->
                            item {
                                AudioPlayerCard(
                                    track = track,
                                    playerState = uiState.playerState,
                                    onPlayPause = { musicViewModel.togglePlayback() },
                                    onSeek = { musicViewModel.seekTo(it) },
                                    onToggleBookmark = { musicViewModel.toggleBookmark(track) },
                                    onVariation = { musicViewModel.createVariation(track) },
                                    onDelete = { musicViewModel.deleteTrack(track) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // History Tab
                    if (uiState.history.isEmpty()) {
                        EmptyStateView(title = "No Music Tracks Generated Yet", subtitle = "Your generated audio compositions will appear here.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.history, key = { it.id }) { track ->
                                TrackHistoryItem(
                                    track = track,
                                    isActive = uiState.currentTrack?.id == track.id,
                                    isPlaying = uiState.currentTrack?.id == track.id && uiState.playerState.isPlaying,
                                    onPlay = { musicViewModel.playTrack(track) },
                                    onToggleBookmark = { musicViewModel.toggleBookmark(track) },
                                    onDelete = { musicViewModel.deleteTrack(track) }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Bookmarks Tab
                    if (uiState.savedTracks.isEmpty()) {
                        EmptyStateView(title = "No Bookmarked Tracks", subtitle = "Bookmark your favorite music compositions to access them quickly.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.savedTracks, key = { it.id }) { track ->
                                TrackHistoryItem(
                                    track = track,
                                    isActive = uiState.currentTrack?.id == track.id,
                                    isPlaying = uiState.currentTrack?.id == track.id && uiState.playerState.isPlaying,
                                    onPlay = { musicViewModel.playTrack(track) },
                                    onToggleBookmark = { musicViewModel.toggleBookmark(track) },
                                    onDelete = { musicViewModel.deleteTrack(track) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayerCard(
    track: MusicResultDomain,
    playerState: com.example.ui.viewmodels.AudioPlayerState,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onVariation: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF231133)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5B8FF).copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("music_player_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.prompt,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${track.genre} • ${track.mood} • ${track.tempoBpm} BPM • ${track.keySignature}",
                        fontSize = 12.sp,
                        color = Color(0xFFF5B8FF)
                    )
                }

                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        if (track.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = Color(0xFFF5B8FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Audio Frequency Visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFF140A1E), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barHeights = listOf(20, 35, 45, 25, 50, 30, 42, 28, 48, 38, 22, 44, 32, 52, 26, 40, 18, 36)
                barHeights.forEachIndexed { idx, height ->
                    val animatedHeight by animateFloatAsState(
                        targetValue = if (playerState.isPlaying) (height * (0.6f + ((idx % 3) * 0.2f))).coerceAtMost(52f) else height * 0.4f,
                        animationSpec = tween(durationMillis = 200), label = "wave"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.5.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (playerState.isPlaying) Color(0xFFF5B8FF) else Color(0xFFF5B8FF).copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seek Slider & Time
            Column {
                Slider(
                    value = playerState.currentPositionMs.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..playerState.durationMs.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFF5B8FF),
                        activeTrackColor = Color(0xFFF5B8FF),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        formatMs(playerState.currentPositionMs),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        formatMs(playerState.durationMs),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onVariation) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Variation", tint = Color.White.copy(alpha = 0.8f))
                }

                FloatingActionButton(
                    onClick = onPlayPause,
                    containerColor = Color(0xFFF5B8FF),
                    contentColor = Color(0xFF0F0C20),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun TrackHistoryItem(
    track: MusicResultDomain,
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF2D183B) else Color(0xFF1B0F2A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0xFFF5B8FF) else Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5B8FF))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF0F0C20)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.prompt,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${track.genre} • ${track.tempoBpm} BPM • ${track.durationSeconds}s",
                    fontSize = 11.sp,
                    color = Color(0xFFF5B8FF)
                )
            }

            IconButton(onClick = onToggleBookmark) {
                Icon(
                    if (track.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Color(0xFFF5B8FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0xFFF5B8FF).copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )
        }
    }
}

fun formatMs(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
