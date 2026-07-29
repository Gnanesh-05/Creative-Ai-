package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.CreativeAITheme
import com.example.ui.viewmodels.*

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val imageViewModel: ImageViewModel by viewModels()
    private val musicViewModel: MusicViewModel by viewModels()
    private val gamesViewModel: GamesViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            CreativeAITheme {
                CreativeAiApp(
                    splashViewModel = splashViewModel,
                    onboardingViewModel = onboardingViewModel,
                    authViewModel = authViewModel,
                    chatViewModel = chatViewModel,
                    imageViewModel = imageViewModel,
                    musicViewModel = musicViewModel,
                    gamesViewModel = gamesViewModel,
                    historyViewModel = historyViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun CreativeAiApp(
    splashViewModel: SplashViewModel,
    onboardingViewModel: OnboardingViewModel,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    imageViewModel: ImageViewModel,
    musicViewModel: MusicViewModel,
    gamesViewModel: GamesViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Chat.route,
        Screen.ImageGenerator.route,
        Screen.MusicComposer.route,
        Screen.GameMind.route,
        Screen.UnifiedHistory.route,
        Screen.Settings.route
    )

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("creative_ai_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigate(Screen.Home.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Chat.route,
                        onClick = { navController.navigate(Screen.Chat.route) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Chat AI") },
                        label = { Text("Chat") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.ImageGenerator.route,
                        onClick = { navController.navigate(Screen.ImageGenerator.route) },
                        icon = { Icon(Icons.Default.Brush, contentDescription = "Image AI") },
                        label = { Text("Studio") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.MusicComposer.route,
                        onClick = { navController.navigate(Screen.MusicComposer.route) },
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = "Music AI") },
                        label = { Text("Music") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.GameMind.route,
                        onClick = { navController.navigate(Screen.GameMind.route) },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Game AI") },
                        label = { Text("Games") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.UnifiedHistory.route,
                        onClick = { navController.navigate(Screen.UnifiedHistory.route) },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    splashViewModel = splashViewModel,
                    onNavigateToDestination = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onboardingViewModel = onboardingViewModel,
                    onFinishOnboarding = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateToResetToken = { navController.navigate(Screen.ResetPassword.route) },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.ResetPassword.route) {
                ResetPasswordScreen(
                    authViewModel = authViewModel,
                    onResetSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(Screen.Chat.route) {
                ChatScreen(chatViewModel = chatViewModel)
            }
            composable(Screen.ImageGenerator.route) {
                ImageGeneratorScreen(imageViewModel = imageViewModel)
            }
            composable(Screen.MusicComposer.route) {
                MusicComposerScreen(musicViewModel = musicViewModel)
            }
            composable(Screen.GameMind.route) {
                GameMindScreen(
                    gamesViewModel = gamesViewModel,
                    onNavigateToGame = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Chess.route) {
                ChessGameScreen(gamesViewModel = gamesViewModel)
            }
            composable(Screen.TicTacToe.route) {
                TicTacToeScreen(gamesViewModel = gamesViewModel)
            }
            composable(Screen.Maze.route) {
                MazeGameScreen(gamesViewModel = gamesViewModel)
            }
            composable(Screen.UnifiedHistory.route) {
                UnifiedHistoryScreen(historyViewModel = historyViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
