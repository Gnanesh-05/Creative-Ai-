package com.example.frontend.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.SplashState
import com.example.frontend.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    onNavigateToDestination: (String) -> Unit,
    splashViewModel: SplashViewModel? = null
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_fade"
    )

    val splashState by splashViewModel?.state?.collectAsState() ?: remember { mutableStateOf(SplashState.Loading) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(splashState) {
        val currentState = splashState
        if (currentState is SplashState.Navigate) {
            onNavigateToDestination(currentState.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8FF))
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            Text(
                text = "PRODUCTION STABLE V1.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEADDFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Creative AI Logo",
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Creative ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF1D1B1E),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "AI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4),
                    letterSpacing = (-0.5).sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Multi-Modal Conversational, Visual & Game Intelligence",
                fontSize = 14.sp,
                color = Color(0xFF49454F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                color = Color(0xFF6750A4),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
