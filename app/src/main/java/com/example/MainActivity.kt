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
import com.example.frontend.viewmodel.AuthViewModel
import com.example.frontend.viewmodel.ChatViewModel
import com.example.frontend.viewmodel.SettingsViewModel
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
import com.example.frontend.screens.RegisterScreen
import com.example.frontend.screens.ForgotPasswordScreen

class MainActivity : ComponentActivity() {
    private val viewModel: NexusViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusAiTheme {
                var appState by remember { mutableStateOf(1) } // 0: Onboarding, 1: Login, 2: Main App, 3: Email Verification, 4: Register, 5: Forgot Password

                when (appState) {
                    0 -> OnboardingScreen(
                        onFinishOnboarding = { appState = 1 }
                    )
                    1 -> LoginScreen(
                        authViewModel = authViewModel,
                        onNavigateToRegister = { appState = 4 },
                        onNavigateToForgotPassword = { appState = 5 },
                        onLoginSuccess = {
                            appState = 2
                        }
                    )
                    3 -> EmailVerificationScreen(
                        onVerifiedSuccess = { appState = 2 },
                        onSignOut = { appState = 1 }
                    )
                    4 -> RegisterScreen(
                        authViewModel = authViewModel,
                        onNavigateToLogin = { appState = 1 },
                        onRegisterSuccess = { appState = 3 }
                    )
                    5 -> ForgotPasswordScreen(
                        authViewModel = authViewModel,
                        onNavigateToResetToken = { appState = 1 },
                        onBackToLogin = { appState = 1 }
                    )
                    else -> MainAppContent(
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        chatViewModel = chatViewModel,
                        settingsViewModel = settingsViewModel,
                        onLogout = { appState = 1 }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: NexusViewModel,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
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
                    onNavigate = { route ->
                        when (route) {
                            "chat" -> selectedTab = 2
                            "image_generator" -> selectedTab = 3
                            "music_composer" -> selectedTab = 3
                            "game_center" -> {
                                selectedGameTab = 0
                                showGameCenter = true
                            }
                        }
                    }
                )
                1 -> VoiceScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 2 },
                    onBack = { selectedTab = 0 },
                    modifier = modifier
                )
                2 -> ChatScreen(
                    chatViewModel = chatViewModel
                )
                3 -> StudioScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 2 },
                    modifier = modifier
                )
                4 -> ProfileScreen(
                    authViewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
