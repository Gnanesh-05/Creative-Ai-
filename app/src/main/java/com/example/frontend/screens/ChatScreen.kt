package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.backend.model.ChatMessage
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.components.GlassCard
import com.example.frontend.components.ThinkingIndicator
import com.example.frontend.theme.IridescentGradient
import com.example.frontend.theme.LightCanvasEnd
import com.example.frontend.theme.LightCanvasMid
import com.example.frontend.theme.LightCanvasStart
import com.example.frontend.theme.LightGlassBorder
import com.example.frontend.theme.LightGlassSurface
import com.example.frontend.theme.PastelMagenta
import com.example.frontend.theme.PastelViolet
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight

@Composable
fun ChatScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val thinkingStatusText by viewModel.thinkingStatusText.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header (Matching Image 3 Layout with Light Glass Aesthetics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryLight)
                }

                Text(
                    text = "Smart Chat",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryLight,
                        fontSize = 17.sp
                    )
                )

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "Menu", tint = TextPrimaryLight)
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (messages.isEmpty()) {
                    // Default Greeting Stream Matching Image 3 Mockup
                    item {
                        SmartChatVoiceBubble(
                            audioDuration = "2:19",
                            timestamp = "8:23 am",
                            isUser = true
                        )
                    }
                    item {
                        SmartChatTextBubble(
                            text = "Sure! Do you want a full brain model or specific part?",
                            timestamp = "8:23 am",
                            isUser = false
                        )
                    }
                    item {
                        SmartChatVoiceBubble(
                            audioDuration = "1:19",
                            timestamp = "8:23 am",
                            isUser = true
                        )
                    }
                    item {
                        SmartChatTextBubble(
                            text = "Got it! Generating a 3D interactive model view just for you...",
                            timestamp = "8:24 am",
                            isUser = false,
                            showMediaCard = true
                        )
                    }
                } else {
                    items(messages) { msg ->
                        SmartChatDynamicMessageItem(message = msg)
                    }
                }

                if (isThinking) {
                    item {
                        ThinkingIndicator(statusText = thinkingStatusText)
                    }
                }
            }

            // Bottom Control Input Bar (Matching Image 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "+" Attachment Pill
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color.White, CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Attach", tint = TextPrimaryLight, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Capsule
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type your message...", color = TextMutedLight, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = TextPrimaryLight,
                        unfocusedTextColor = TextPrimaryLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Glowing Gradient Send Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IridescentGradient)
                        .clickable {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SmartChatVoiceBubble(
    audioDuration: String,
    timestamp: String,
    isUser: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PastelViolet),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iridescent_light_orb_1785680344374),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isUser) PastelViolet.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f))
                    .border(1.dp, Color.White, RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isUser) Color.White else TextPrimaryLight)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Audio",
                            tint = if (isUser) TextPrimaryLight else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Waveform lines visualizer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(100.dp)
                    ) {
                        val heights = listOf(8, 14, 20, 10, 18, 24, 12, 16, 22, 10, 15, 8)
                        heights.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) PastelMagenta else if (isUser) Color.White else TextPrimaryLight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = audioDuration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isUser) Color.White else TextSecondaryLight,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMutedLight, fontSize = 10.sp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "User Avatar", tint = TextPrimaryLight, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SmartChatTextBubble(
    text: String,
    timestamp: String,
    isUser: Boolean,
    showMediaCard: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PastelViolet),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iridescent_light_orb_1785680344374),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            GlassCard(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 20.dp
                ),
                backgroundColor = if (isUser) PastelViolet.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
                borderColor = LightGlassBorder
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isUser) Color.White else TextPrimaryLight,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    if (showMediaCard) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.iridescent_light_orb_1785680344374),
                                contentDescription = "Generated 3D Model",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action icons row matching Image 3 (copy, thumbs up, speaker, refresh)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondaryLight, modifier = Modifier.size(16.dp))
                            Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Like", tint = TextSecondaryLight, modifier = Modifier.size(16.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read Aloud", tint = PastelMagenta, modifier = Modifier.size(16.dp))
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate", tint = TextSecondaryLight, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMutedLight, fontSize = 10.sp),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun SmartChatDynamicMessageItem(message: ChatMessage) {
    val isUser = message.sender == "USER"
    SmartChatTextBubble(
        text = message.content,
        timestamp = "Just now",
        isUser = isUser,
        showMediaCard = message.content.contains("3D") || message.content.contains("image") || message.content.contains("model")
    )
}

