package com.example.frontend.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.components.GlassCard
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
import com.example.backend.util.App3DAssets

data class NarrativePage(
    val title: String,
    val subtitle: String,
    val description: String,
    val imageRes: Int,
    val badge: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        NarrativePage(
            title = "Welcome to Nexus AI",
            subtitle = "3D Next-Gen Intelligence Suite",
            description = "Unleash real-time voice conversations, intelligent assistant personas, photorealistic image creation, and music synthesis in one place.",
            imageRes = App3DAssets.appLogo,
            badge = "✨ Neural Engine 3.0"
        ),
        NarrativePage(
            title = "Voice & Conversational AI",
            subtitle = "Fluid Real-Time Dialogue",
            description = "Speak naturally with instant low-latency voice responses and custom AI assistant models trained for work, reasoning, and creativity.",
            imageRes = App3DAssets.voiceMic,
            badge = "🎙️ Real-Time Voice"
        ),
        NarrativePage(
            title = "Visuals & Music Synthesis",
            subtitle = "Turn Words into Art & Songs",
            description = "Generate 100% precision visual artwork and complete songs with verse, lyrics, chords, and playable audio tracks in seconds.",
            imageRes = App3DAssets.musicClef,
            badge = "🎵 Instant Synthesis"
        )
    )

    // Infinite Breathing Float Animation for 3D Asset
    val infiniteTransition = rememberInfiniteTransition(label = "FloatAnim")
    val floatScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Progress Indicator + Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.indices.forEach { index ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 28.dp else 10.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PastelViolet else Color.White.copy(alpha = 0.6f))
                        )
                    }
                }

                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PastelViolet,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onFinishOnboarding() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Animated Content Section
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "NarrativeContent"
            ) { pageIdx ->
                val page = pages[pageIdx]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 3D Animated Hero Illustration Frame
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .graphicsLayer {
                                scaleX = floatScale
                                scaleY = floatScale
                            }
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(2.dp, Color.White, RoundedCornerShape(32.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = page.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Narrative Text Glass Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = LightGlassSurface,
                        borderColor = LightGlassBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PastelViolet.copy(alpha = 0.15f))
                                    .border(1.dp, PastelViolet.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = page.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PastelViolet
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = page.subtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PastelMagenta,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = page.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryLight,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Navigation Action Button
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PastelViolet),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1) "Next Feature" else "Get Started / Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentPage < pages.size - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
