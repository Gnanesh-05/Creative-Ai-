package com.example.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.AuthViewModel
import com.example.frontend.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val profileState by settingsViewModel.profileUiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf(profileState.profile.fullName) }
    var editUsername by remember { mutableStateOf(profileState.profile.username) }
    var editBio by remember { mutableStateOf(profileState.profile.bio) }

    LaunchedEffect(profileState.profile) {
        editFullName = profileState.profile.fullName
        editUsername = profileState.profile.username
        editBio = profileState.profile.bio
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            icon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFD0BCFF)) },
            title = { Text("Edit Profile Details", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("profile_edit_fullname_input")
                    )
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("profile_edit_username_input")
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Short Bio") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("profile_edit_bio_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateProfile(editFullName, editUsername, editBio)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.testTag("profile_save_changes_button")
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    modifier = Modifier.testTag("profile_cancel_edit_button")
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1938),
            modifier = Modifier.testTag("profile_edit_dialog")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("profile_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "User Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("profile_open_edit_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFFD0BCFF))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Avatar & Name Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4))
                            .border(3.dp, Color(0xFFD0BCFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profileState.profile.fullName.ifEmpty { "Creative Master" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "@${profileState.profile.username.ifEmpty { authState.username.ifEmpty { "creator" } }}",
                        fontSize = 13.sp,
                        color = Color(0xFFD0BCFF)
                    )

                    Text(
                        text = profileState.profile.email.ifEmpty { authState.email.ifEmpty { "user@creativeai.app" } },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = profileState.profile.bio.ifEmpty { "AI Enthusiast & Game Creator" },
                        fontSize = 12.sp,
                        color = Color(0xFFCCC2DC),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Membership & Quota Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4FD8EB).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF4FD8EB))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = profileState.profile.tier,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Member since ${profileState.profile.accountCreated}",
                                fontSize = 12.sp,
                                color = Color(0xFFCCC2DC)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily AI Quota Used", fontSize = 13.sp, color = Color(0xFFCCC2DC))
                        Text(
                            "${profileState.profile.dailyGenerationsUsed} / ${profileState.profile.dailyGenerationsMax}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (profileState.profile.dailyGenerationsUsed.toFloat() / profileState.profile.dailyGenerationsMax.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFD0BCFF),
                        trackColor = Color(0xFF49454F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = { showEditDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2544)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_edit_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFD0BCFF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile Information", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_logout_button")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out & Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
