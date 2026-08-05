package com.example.frontend.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.components.AudioWaveVisualizer
import com.example.frontend.components.GlassCard
import com.example.frontend.theme.IridescentGradient
import com.example.frontend.theme.LightCanvasEnd
import com.example.frontend.theme.LightCanvasMid
import com.example.frontend.theme.LightCanvasStart
import com.example.frontend.theme.NexusCyan
import com.example.frontend.theme.NexusMagenta
import com.example.frontend.theme.NexusViolet
import com.example.frontend.theme.PastelMagenta
import com.example.frontend.theme.PastelViolet
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight
import com.example.backend.util.VoiceInteractionManager

@Composable
fun VoiceScreen(
    viewModel: NexusViewModel,
    onNavigateToChat: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceInteractionManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    val isListening by voiceManager.isListening.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val lastAiVoiceResponse by voiceManager.lastAiVoiceResponse.collectAsState()

    val isThinking by viewModel.isThinking.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    var manualTextInput by remember { mutableStateOf("") }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            voiceManager.startListening { text ->
                viewModel.sendMessage(text)
            }
        }
    }

    // Auto-speak latest AI response when generated
    LaunchedEffect(chatMessages, isThinking) {
        if (!isThinking && chatMessages.isNotEmpty()) {
            val lastMsg = chatMessages.last()
            if (lastMsg.sender == "ASSISTANT" && lastMsg.content.isNotBlank()) {
                if (lastMsg.content != lastAiVoiceResponse) {
                    voiceManager.speak(lastMsg.content)
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "OrbVoicePulse")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbVoiceFloatY"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isListening || isSpeaking) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 800 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbVoiceScale"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isListening || isSpeaking) 1.35f else 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraVoiceScale"
    )

    val rotationDegree by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    val scrollState = rememberScrollState()

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryLight)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Voice",
                        tint = NexusViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-time Voice Chat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight,
                            fontSize = 17.sp
                        )
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = TextPrimaryLight)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Central Voice AI Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Assistant Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when {
                                isSpeaking -> NexusMagenta.copy(alpha = 0.15f)
                                isListening -> NexusViolet.copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.8f)
                            }
                        )
                        .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isSpeaking) NexusMagenta else NexusViolet,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isSpeaking -> "🔊 AI Speaking..."
                                isThinking -> "🧠 AI Gathering..."
                                isListening -> "🎙️ Listening to Voice..."
                                else -> "✨ Voice AI Ready"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextPrimaryLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Header
                Text(
                    text = when {
                        isSpeaking -> "Speaking Response..."
                        isThinking -> "Gathering Answer..."
                        isListening -> "Listening to you..."
                        else -> "Tap Microphone to Speak"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3D Animated Orb Visualizer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(230.dp)
                ) {
                    // Outer Pulsing Aura (Hardware accelerated)
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .graphicsLayer {
                                scaleX = auraPulse
                                scaleY = auraPulse
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        (if (isSpeaking) NexusMagenta else NexusViolet).copy(alpha = 0.5f),
                                        PastelViolet.copy(alpha = 0.3f),
                                        Color(0xFFC084FC).copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Secondary Glow Ring with Continuous 360-Degree Rotation
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                scaleX = scalePulse
                                scaleY = scalePulse
                                rotationZ = rotationDegree
                            }
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        PastelMagenta,
                                        PastelViolet,
                                        NexusCyan,
                                        PastelMagenta
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Center 3D Iridescent Orb
                    Image(
                        painter = painterResource(id = R.drawable.iridescent_light_orb_1785680344374),
                        contentDescription = "Voice AI Orb",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(175.dp)
                            .graphicsLayer {
                                translationY = floatY * 2f
                                scaleX = scalePulse
                                scaleY = scalePulse
                            }
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                if (isListening) {
                                    voiceManager.stopListening()
                                } else if (isSpeaking) {
                                    voiceManager.stopSpeaking()
                                } else {
                                    if (hasMicPermission) {
                                        voiceManager.startListening { query ->
                                            viewModel.sendMessage(query)
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Audio Wave Visualizer
                AudioWaveVisualizer(isPlaying = isListening || isSpeaking || isThinking)

                Spacer(modifier = Modifier.height(12.dp))

                // Live Voice-to-Text Transcript Display Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color.White.copy(alpha = 0.85f),
                    borderColor = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        if (spokenText.isNotBlank()) {
                            Text(
                                text = "YOU SAID (VOICE TO TEXT):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NexusViolet
                            )
                            Text(
                                text = spokenText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimaryLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (lastAiVoiceResponse.isNotBlank()) {
                            Text(
                                text = "AI SPOKEN RESPONSE (TEXT TO VOICE):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NexusMagenta
                            )
                            Text(
                                text = lastAiVoiceResponse,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimaryLight,
                                maxLines = 5
                            )
                        }

                        if (spokenText.isBlank() && lastAiVoiceResponse.isBlank()) {
                            Text(
                                text = "Speech recognition active. Tap mic and speak e.g. \"Explain Quantum Computing\" or \"Tell me about mangoes\"",
                                fontSize = 12.sp,
                                color = TextSecondaryLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voice Preset Prompts Row
                Text(
                    text = "Quick Voice Prompts:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryLight,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                val voicePresets = listOf(
                    "Tell me about mangoes",
                    "How can I reduce stress today?",
                    "Explain Quantum Computing simply",
                    "Give me a soothing story"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(voicePresets) { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.sendMessage(preset)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Input / Trigger Bar
            OutlinedTextField(
                value = manualTextInput,
                onValueChange = { manualTextInput = it },
                placeholder = { Text("Type voice prompt or speak above...", fontSize = 12.sp, color = TextMutedLight) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = NexusViolet,
                    unfocusedBorderColor = Color.White
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (manualTextInput.isNotBlank()) {
                                viewModel.sendMessage(manualTextInput)
                                manualTextInput = ""
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NexusViolet)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (manualTextInput.isNotBlank()) {
                            viewModel.sendMessage(manualTextInput)
                            manualTextInput = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch to Text Chat Button
                IconButton(
                    onClick = onNavigateToChat,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Text Chat",
                        tint = TextPrimaryLight,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Central Main Microphone Toggle Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(IridescentGradient)
                        .clickable {
                            if (isListening) {
                                voiceManager.stopListening()
                            } else if (isSpeaking) {
                                voiceManager.stopSpeaking()
                            } else {
                                if (hasMicPermission) {
                                    voiceManager.startListening { query ->
                                        viewModel.sendMessage(query)
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (isListening || isSpeaking) NexusMagenta else Color.White.copy(alpha = 0.25f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Close Voice Chat Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimaryLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
