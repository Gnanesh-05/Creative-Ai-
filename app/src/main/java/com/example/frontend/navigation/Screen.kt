package com.example.frontend.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Splash")
    object Onboarding : Screen("onboarding", "Onboarding")
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object ForgotPassword : Screen("forgot_password", "Forgot Password")
    object ResetPassword : Screen("reset_password", "Reset Password")
    
    // Main Dashboard & Navigation Bar Screens
    object Home : Screen("home", "Home")
    object Chat : Screen("chat", "Chat AI")
    object ImageGenerator : Screen("image_generator", "AI Studio Image")
    object MusicComposer : Screen("music_composer", "AI Music")
    object GameMind : Screen("game_mind", "Game Mind AI")
    object Chess : Screen("chess", "Chess vs AI")
    object TicTacToe : Screen("tictactoe", "Tic-Tac-Toe")
    object Maze : Screen("maze", "AI Maze Pathfinder")
    object UnifiedHistory : Screen("unified_history", "History")
    object Profile : Screen("profile", "Profile")
    object Settings : Screen("settings", "Settings")
}
