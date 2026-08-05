package com.example.frontend.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.OnboardingViewModel

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    onboardingViewModel: OnboardingViewModel? = null
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Welcome to Creative AI",
                subtitle = "Multi-Modal Intelligence Workspace",
                description = "Experience unified AI capabilities combining conversational language models, photorealistic studio generation, audio synthesis, and tactical game mind agents.",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFF6750A4)
            ),
            OnboardingPageData(
                title = "Conversational Chat AI",
                subtitle = "Gemini LLM Reasoning Engine",
                description = "Engage in intelligent dialogue for code generation, complex problem solving, document synthesis, and deep creative brainstorming.",
                icon = Icons.Default.Chat,
                accentColor = Color(0xFF006874)
            ),
            OnboardingPageData(
                title = "Studio & Music Composer",
                subtitle = "Visual Artwork & Audio Synthesis",
                description = "Transform text prompts into high-resolution images, customizable style presets, and real-time synthesized ambient music melodies.",
                icon = Icons.Default.Brush,
                accentColor = Color(0xFF984061)
            ),
            OnboardingPageData(
                title = "Game Mind AI",
                subtitle = "Chess, Tic-Tac-Toe & AI Maze",
                description = "Test your strategy against adaptive AI in tactical Chess with positional analysis, smart Tic-Tac-Toe, and procedurally solved mazes.",
                icon = Icons.Default.SportsEsports,
                accentColor = Color(0xFF8C4E2A)
            )
        )
    }

    val pageIndexFromVm by onboardingViewModel?.currentPage?.collectAsState() ?: remember { mutableStateOf(0) }
    var localPageIndex by remember { mutableStateOf(0) }
    
    val currentPage = if (onboardingViewModel != null) pageIndexFromVm else localPageIndex

    fun completeAndNavigate() {
        onboardingViewModel?.completeOnboarding()
        onFinishOnboarding()
    }

    fun goToNext() {
        if (currentPage < pages.size - 1) {
            if (onboardingViewModel != null) {
                onboardingViewModel.nextPage()
            } else {
                localPageIndex++
            }
        } else {
            completeAndNavigate()
        }
    }

    fun goToPrevious() {
        if (currentPage > 0) {
            if (onboardingViewModel != null) {
                onboardingViewModel.previousPage()
            } else {
                localPageIndex--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8FF))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("onboarding_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation Bar (Back & Skip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    IconButton(
                        onClick = { goToPrevious() },
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF49454F)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                if (currentPage < pages.size - 1) {
                    TextButton(
                        onClick = { completeAndNavigate() },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            color = Color(0xFF49454F),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Middle Content - Editorial Animated Card
            val page = pages[currentPage]
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "onboarding_page_transition"
            ) { currentPageData ->
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEADDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentPageData.icon,
                                contentDescription = currentPageData.title,
                                tint = currentPageData.accentColor,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = currentPageData.subtitle.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentPageData.accentColor,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentPageData.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B1E),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentPageData.description,
                            fontSize = 14.sp,
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Bottom Navigation Indicators & Next/Get Started Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .height(8.dp)
                                .width(if (index == currentPage) 28.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPage) Color(0xFF6750A4) else Color(0xFFCAC4D0)
                                )
                                .clickable {
                                    if (onboardingViewModel != null) {
                                        onboardingViewModel.setPage(index)
                                    } else {
                                        localPageIndex = index
                                    }
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { goToNext() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1) "Continue" else "Get Started",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
