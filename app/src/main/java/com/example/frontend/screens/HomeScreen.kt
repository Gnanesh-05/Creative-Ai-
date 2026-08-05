package com.example.frontend.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.components.GlassCard
import com.example.frontend.theme.IridescentGradient
import com.example.frontend.theme.LightCanvasEnd
import com.example.frontend.theme.LightCanvasMid
import com.example.frontend.theme.LightCanvasStart
import com.example.frontend.theme.LightGlassBorder
import com.example.frontend.theme.LightGlassSurface
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight
import com.example.backend.util.App3DAssets

@Composable
fun HomeScreen(
    viewModel: NexusViewModel,
    onNavigateToVoice: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToGames: () -> Unit = {},
    onNavigateToGameTab: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchPrompt by remember { mutableStateOf("") }
    var showProfileSheet by remember { mutableStateOf(false) }

    // Floating animation for the 3D Orb and Action Logos
    val infiniteTransition = rememberInfiniteTransition(label = "OrbHomeFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbFloatY"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScalePulse"
    )

    val rotationDegree by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HomeRotation"
    )

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar: 3D App Logo Badge + CREATIVE AI Branding Header + Notification Bell
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("top-nav-bar"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(2.dp, Color(0xFFC084FC), CircleShape)
                            .clickable { showProfileSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = App3DAssets.appLogo),
                            contentDescription = "User Profile & Email Info",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                                .border(1.5.dp, Color.White, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }

                    // Corporate Tech Branding Title: Extended Sans-Serif, ALL-CAPS, Generous Letter Spacing, Glowing Deep Purple AI Accent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CREATIVE ",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 4.sp,
                                color = TextPrimaryLight
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF7C3AED), // Glowing Deep Purple
                                            Color(0xFFC084FC)  // Pastel Violet Accent
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 3.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Right Side: Notification Bell
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = TextPrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Header Greeting & Subtitle
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hi Amelia,",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight,
                            fontSize = 28.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ask any questions — your Creative AI assistant is always ready.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Center Floating Animated 3D App Logo
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft background glowing purple radial aura (hardware accelerated)
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                scaleX = scalePulse
                                scaleY = scalePulse
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6).copy(alpha = 0.45f),
                                        Color(0xFFA855F7).copy(alpha = 0.25f),
                                        Color(0xFFC084FC).copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Floating 3D Glossy App Logo Container (GPU graphicsLayer optimized for fast scrolling)
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .graphicsLayer {
                                translationY = floatY * 2.5f
                                scaleX = scalePulse
                                scaleY = scalePulse
                            }
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color.White)
                            .border(2.dp, Color.White, RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = App3DAssets.appLogo),
                            contentDescription = "3D Creative AI Intelligence Core",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(36.dp))
                        )
                    }
                }
            }

            // 6 Frosted Glass Quick Action Tiles Grid with 3D Logos (Including AI Game Center)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GlassActionCard3D(
                        title = "Voice\nChat AI",
                        imageRes = App3DAssets.voiceMic,
                        scalePulseProvider = { scalePulse },
                        floatYProvider = { floatY },
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVoice
                    )
                    GlassActionCard3D(
                        title = "Chat\nwith AI",
                        imageRes = App3DAssets.chatbot,
                        scalePulseProvider = { scalePulse },
                        floatYProvider = { -floatY },
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToChat
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GlassActionCard3D(
                        title = "AI Game\nCenter 🎮",
                        imageRes = com.example.R.drawable.img_game_center_banner,
                        scalePulseProvider = { scalePulse },
                        floatYProvider = { floatY },
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGames
                    )
                    GlassActionCard3D(
                        title = "Generate\nImages",
                        imageRes = App3DAssets.imageGen,
                        scalePulseProvider = { scalePulse },
                        floatYProvider = { -floatY },
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudio
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GlassActionCard3D(
                        title = "Music &\nLyrics",
                        imageRes = App3DAssets.musicClef,
                        scalePulseProvider = { scalePulse },
                        floatYProvider = { floatY },
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudio
                    )
                    GlassActionCard3D(
                        title = "AI Agents\nHub 🤖",
                        imageRes = App3DAssets.appLogo,
                        scalePulseProvider = { scalePulse },
                        floatYProvider = { -floatY },
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAgents
                    )
                }
            }

            // AI Game Center Featured Banner Card & Quick Play Games
            item {
                Spacer(modifier = Modifier.height(14.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToGames() },
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.White.copy(alpha = 0.85f),
                    borderColor = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black)
                            ) {
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.img_game_center_banner),
                                    contentDescription = "AI Game Center",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AI GAME CENTER",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimaryLight,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(IridescentGradient)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PLAY NOW",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Play Tic-Tac-Toe, Chess vs AI & Maze Game in real-time",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = onNavigateToGames,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "Play AI Games",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Quick Play Real-Time Games Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Game 1: Tic Tac Toe
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF3E8FF))
                                    .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(14.dp))
                                    .clickable { onNavigateToGameTab(0) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("❌ ⭕", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Tic-Tac-Toe",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6B21A8)
                                    )
                                    Text(
                                        "Play vs AI",
                                        fontSize = 9.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            // Game 2: Chess vs AI
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFEDE9FE))
                                    .border(1.dp, Color(0xFFC4B5FD), RoundedCornerShape(14.dp))
                                    .clickable { onNavigateToGameTab(1) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("♟️ 👑", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Chess vs AI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF5B21B6)
                                    )
                                    Text(
                                        "Strategy",
                                        fontSize = 9.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            // Game 3: Maze Game
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFAE8FF))
                                    .border(1.dp, Color(0xFFF5D0FE), RoundedCornerShape(14.dp))
                                    .clickable { onNavigateToGameTab(2) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🧩 🌀", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Maze Game",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF86198F)
                                    )
                                    Text(
                                        "Labyrinth",
                                        fontSize = 9.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom "Ask anything..." Frosted Glass Capsule Bar
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchPrompt,
                            onValueChange = { searchPrompt = it },
                            placeholder = {
                                Text(
                                    "Ask anything...",
                                    color = TextMutedLight,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimaryLight,
                                unfocusedTextColor = TextPrimaryLight
                            ),
                            singleLine = true
                        )

                        // Iridescent Gradient Round Action Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(IridescentGradient)
                                .clickable {
                                    if (searchPrompt.isNotBlank()) {
                                        viewModel.sendMessage(searchPrompt)
                                        searchPrompt = ""
                                        onNavigateToChat()
                                    } else {
                                        onNavigateToVoice()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Submit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showProfileSheet) {
            UserProfileHistoryBottomSheet(
                onDismiss = { showProfileSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileHistoryBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF9F7FD),
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // User Header Profile & Email Info Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = Color(0xFFE9D5FF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(IridescentGradient)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = App3DAssets.appLogo),
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bharath Vijayakumar",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = TextPrimaryLight
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email ID row with Copy Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3E8FF))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("bharath27vijayakumari02@gmail.com"))
                                    Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "bharath27vijayakumari02@gmail.com",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B21A8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time History & Activity List Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = Color(0xFFE9D5FF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Real-time Activity & Prompt History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimaryLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // History Items
                    val historyList = listOf(
                        Triple("❌⭕ Tic-Tac-Toe Game", "Won vs AI (3 - 1)", "2m ago"),
                        Triple("♟️ Chess vs AI", "Checkmate in 14 moves", "12m ago"),
                        Triple("🎨 Image Generation", "8K Cyberpunk City Wallpaper", "25m ago"),
                        Triple("🎙️ Voice AI Assistant", "Real-time Speech Session", "1h ago"),
                        Triple("🧩 Mind Maze Game", "Labyrinth Level 2 Cleared", "2h ago")
                    )

                    historyList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = item.second,
                                    fontSize = 10.sp,
                                    color = TextSecondaryLight
                                )
                            }
                            Text(
                                text = item.third,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                        if (index < historyList.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFF3E8FF))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time System Performance & Latency Metrics Card (Real-time Feature 1)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = Color(0xFFE9D5FF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Metrics",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-time Engine & System Metrics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricTile("Latency", "18 ms", Color(0xFF10B981), Modifier.weight(1f))
                        MetricTile("Model", "Nexus 3.5", Color(0xFF7C3AED), Modifier.weight(1f))
                        MetricTile("Cache", "4.2 MB", Color(0xFF3B82F6), Modifier.weight(1f))
                        MetricTile("Encryption", "E2E Safe", Color(0xFF8B5CF6), Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Management Actions (Real-time Feature 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFEE2E2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))
                        .clickable {
                            Toast.makeText(context, "Cache and prompt history cleared!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Cache",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Clear Cache",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEDE9FE))
                        .border(1.dp, Color(0xFFC4B5FD), RoundedCornerShape(16.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString("Bharath Vijayakumar (bharath27vijayakumari02@gmail.com) - Activity History: Tic-Tac-Toe, Chess, Image Studio, Voice AI"))
                            Toast.makeText(context, "Activity log exported to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export History",
                            tint = Color(0xFF6D28D9),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export Log",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF5B21B6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.1f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextSecondaryLight
            )
        }
    }
}

@Composable
fun GlassActionCard3D(
    title: String,
    imageRes: Int,
    scalePulseProvider: () -> Float,
    floatYProvider: () -> Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CardPressScale"
    )

    Box(
        modifier = modifier
            .height(168.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        // Full cover 3D Image Asset with GPU accelerated float & scale pulse animation
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = floatYProvider() * 1.5f
                    scaleX = scalePulseProvider()
                    scaleY = scalePulseProvider()
                }
                .clip(RoundedCornerShape(28.dp))
        )

        // Subtle gradient overlay at bottom for title text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.65f)
                        ),
                        startY = 80f
                    )
                )
        )

        // Title Text placed in bottom-left corner with bold, modern styling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    color = Color.White
                )
            )
        }
    }
}


