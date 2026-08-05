package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.screens.ChatScreen
import com.example.frontend.screens.HomeScreen
import com.example.frontend.screens.ProfileScreen
import com.example.frontend.screens.StudioScreen
import com.example.frontend.screens.VoiceScreen
import com.example.frontend.theme.NexusAiTheme
import com.example.frontend.theme.NexusMagenta
import com.example.frontend.theme.NexusViolet

import com.example.frontend.screens.EmailVerificationScreen
import com.example.frontend.screens.GameCenterScreen
import com.example.frontend.screens.LoginScreen
import com.example.frontend.screens.OnboardingScreen

class MainActivity : ComponentActivity() {
    private val viewModel: NexusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusAiTheme {
                var appState by remember { mutableStateOf(1) } // 0: Onboarding, 1: Login, 2: Main App, 3: Email Verification

                when (appState) {
                    0 -> OnboardingScreen(
                        onFinishOnboarding = { appState = 1 }
                    )
                    1 -> LoginScreen(
                        onLoginSuccess = { userName, userEmail ->
                            appState = 2
                        }
                    )
                    3 -> EmailVerificationScreen(
                        onVerifiedSuccess = { appState = 2 },
                        onSignOut = { appState = 1 }
                    )
                    else -> MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: NexusViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Voice, 2: Chat, 3: Studio, 4: Profile
    var showGameCenter by remember { mutableStateOf(false) }
    var selectedGameTab by remember { mutableStateOf(0) }

    if (showGameCenter) {
        GameCenterScreen(
            initialTab = selectedGameTab,
            onBack = { showGameCenter = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xF0FFFFFF),
                    tonalElevation = 12.dp
                ) {
                    val navItems = listOf(
                        Triple("Home", Icons.Default.Home, 0),
                        Triple("Voice", Icons.Default.Mic, 1),
                        Triple("Smart Chat", Icons.Default.ChatBubble, 2),
                        Triple("Studio", Icons.Default.Brush, 3),
                        Triple("Profile", Icons.Default.Person, 4)
                    )

                    navItems.forEach { (label, icon, index) ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NexusViolet,
                                selectedTextColor = NexusViolet,
                                indicatorColor = NexusViolet.copy(alpha = 0.15f),
                                unselectedIconColor = Color(0xFF81749E),
                                unselectedTextColor = Color(0xFF81749E)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToVoice = { selectedTab = 1 },
                    onNavigateToChat = { selectedTab = 2 },
                    onNavigateToStudio = { selectedTab = 3 },
                    onNavigateToAgents = { selectedTab = 3 },
                    onNavigateToGames = {
                        selectedGameTab = 0
                        showGameCenter = true
                    },
                    onNavigateToGameTab = { gameIndex ->
                        selectedGameTab = gameIndex
                        showGameCenter = true
                    },
                    modifier = modifier
                )
                1 -> VoiceScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 2 },
                    onBack = { selectedTab = 0 },
                    modifier = modifier
                )
                2 -> ChatScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = 0 },
                    modifier = modifier
                )
                3 -> StudioScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 2 },
                    modifier = modifier
                )
                4 -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { selectedTab = 0 },
                    modifier = modifier
                )
            }
        }
    }
}
