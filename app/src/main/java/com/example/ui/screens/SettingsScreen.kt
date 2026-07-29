package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val settingsState by settingsViewModel.settingsUiState.collectAsState()
    val settings = settingsState.settings

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteConfirmPassword by remember { mutableStateOf("") }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD0BCFF)) },
            title = { Text("Change Account Password", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("settings_current_password_input")
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("settings_new_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPasswordInput.isNotEmpty() && newPasswordInput.isNotEmpty()) {
                            settingsViewModel.changePassword(currentPasswordInput, newPasswordInput)
                            showChangePasswordDialog = false
                            currentPasswordInput = ""
                            newPasswordInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.testTag("settings_confirm_change_password_button")
                ) {
                    Text("Update Password", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangePasswordDialog = false },
                    modifier = Modifier.testTag("settings_cancel_change_password_button")
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1938),
            modifier = Modifier.testTag("settings_change_password_dialog")
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Account Permanently?", color = MaterialTheme.colorScheme.error) },
            text = {
                Column {
                    Text(
                        "This action is irreversible. All your generated images, music tracks, chat history, and game scores will be permanently removed.",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deleteConfirmPassword,
                        onValueChange = { deleteConfirmPassword = it },
                        label = { Text("Confirm with Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_delete_account_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmPassword.isNotEmpty()) {
                            settingsViewModel.deleteAccount(deleteConfirmPassword)
                            showDeleteAccountDialog = false
                            authViewModel.logout()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("settings_confirm_delete_account_button")
                ) {
                    Text("Permanently Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    modifier = Modifier.testTag("settings_cancel_delete_account_button")
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1938),
            modifier = Modifier.testTag("settings_delete_account_dialog")
        )
    }

    // Terms, Privacy & About Dialogs
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Creative AI", color = Color.White) },
            text = {
                Text(
                    "Creative AI Suite v1.0.0\n\nBuilt with Kotlin Jetpack Compose, FastAPI, SQLAlchemy & Gemini Flash 2.0. Offers unified AI tools & retro-arcade intelligent games.",
                    color = Color(0xFFCCC2DC)
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close", color = Color(0xFFD0BCFF)) }
            },
            containerColor = Color(0xFF1E1938)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", color = Color.White) },
            text = {
                Text(
                    "By using Creative AI, you agree not to generate illegal content or bypass security bounds. All generated media is subject to platform usage rights.",
                    color = Color(0xFFCCC2DC)
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Agree", color = Color(0xFFD0BCFF)) }
            },
            containerColor = Color(0xFF1E1938)
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = Color.White) },
            text = {
                Text(
                    "We value your privacy. Your data is protected with ownership checks and encrypted JWT tokens. We do not sell your personal activity or generated media.",
                    color = Color(0xFFCCC2DC)
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Understood", color = Color(0xFFD0BCFF)) }
            },
            containerColor = Color(0xFF1E1938)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("settings_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("App Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Preferences, Security & AI Customization", fontSize = 12.sp, color = Color(0xFFD0BCFF))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: Theme & Display
            Text("THEME & DISPLAY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Application Theme", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (themeKey, label) ->
                            FilterChip(
                                selected = settings.theme == themeKey,
                                onClick = { settingsViewModel.updateTheme(themeKey) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2544),
                                    labelColor = Color.LightGray
                                ),
                                modifier = Modifier.testTag("theme_chip_$themeKey")
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFD0BCFF))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Push & In-App Notifications", color = Color.White)
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { settingsViewModel.updateNotifications(it) },
                            modifier = Modifier.testTag("settings_notifications_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

                    Text("Language Architecture", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("English", "Spanish", "French", "German", "Japanese").forEach { lang ->
                            FilterChip(
                                selected = settings.language == lang,
                                onClick = { settingsViewModel.updateLanguage(lang) },
                                label = { Text(lang, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4FD8EB),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF2B2544),
                                    labelColor = Color.LightGray
                                ),
                                modifier = Modifier.testTag("lang_chip_$lang")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: AI Preferences
            Text("AI ENGINE PREFERENCES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chat Response Style", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Detailed & Creative", "Concise", "Code Focus").forEach { style ->
                            FilterChip(
                                selected = settings.aiPreferences.chatResponseStyle == style,
                                onClick = { settingsViewModel.updateAiPreferences(chatStyle = style) },
                                label = { Text(style, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2544)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Default Music Genre", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Ambient Synthwave", "Cinematic", "Lo-Fi Beats").forEach { genre ->
                            FilterChip(
                                selected = settings.aiPreferences.musicGenerationGenre == genre,
                                onClick = { settingsViewModel.updateAiPreferences(musicGenre = genre) },
                                label = { Text(genre, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2544)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: Game Preferences
            Text("GAME & SIMULATION PREFERENCES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chess Difficulty", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Beginner", "Intermediate", "Grandmaster Mind").forEach { diff ->
                            FilterChip(
                                selected = settings.gamePreferences.chessDifficulty == diff,
                                onClick = { settingsViewModel.updateGamePreferences(chessDiff = diff) },
                                label = { Text(diff, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2B2544)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Game Coach Feedback", color = Color.White)
                        Switch(
                            checked = settings.gamePreferences.aiCoachingEnabled,
                            onCheckedChange = { settingsViewModel.updateGamePreferences(coaching = it) },
                            modifier = Modifier.testTag("settings_ai_coaching_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No-Spoiler Game Mode", color = Color.White)
                        Switch(
                            checked = settings.gamePreferences.noSpoilerMode,
                            onCheckedChange = { settingsViewModel.updateGamePreferences(noSpoiler = it) },
                            modifier = Modifier.testTag("settings_no_spoiler_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: Security & Privacy
            Text("SECURITY & ACCOUNT PRIVACY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showChangePasswordDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2544)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_change_password_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD0BCFF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Password", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_delete_account_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 5: Legal & Information
            Text("LEGAL & ABOUT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FD8EB))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD0BCFF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("About Creative AI Suite", color = Color.White)
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFD0BCFF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Terms of Service", color = Color.White)
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = Color(0xFFD0BCFF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Privacy Policy", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
