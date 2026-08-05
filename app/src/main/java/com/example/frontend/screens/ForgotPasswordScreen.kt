package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateToResetToken: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validateEmail(input: String): Boolean {
        return if (input.contains("@") && input.contains(".")) {
            emailError = null
            true
        } else {
            emailError = "Please enter a valid email address."
            false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8FF))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("forgot_password_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6750A4).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Lock Reset Icon",
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Recover Password", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B1E))
            Text(
                "Enter your account email below. We'll generate a secure single-use reset code.",
                fontSize = 13.sp,
                color = Color(0xFF49454F),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) validateEmail(it)
                },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6750A4)) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6750A4),
                    unfocusedBorderColor = Color(0xFFCAC4D0),
                    focusedLabelColor = Color(0xFF6750A4),
                    unfocusedLabelColor = Color(0xFF49454F),
                    focusedTextColor = Color(0xFF1D1B1E),
                    unfocusedTextColor = Color(0xFF1D1B1E)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_password_email_input")
            )

            AnimatedVisibility(visible = uiState.infoMessage != null) {
                uiState.infoMessage?.let { info ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("forgot_password_success_message")
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF6750A4))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = info, color = Color(0xFF1D192B), fontSize = 13.sp)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("forgot_password_error_message")
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = err, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validateEmail(email)) {
                        authViewModel.sendPasswordReset(email)
                    }
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("forgot_password_send_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Request Reset Code", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToResetToken,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("forgot_password_has_token_button")
            ) {
                Text("Enter Reset Code / Token", color = Color(0xFF6750A4), fontWeight = FontWeight.SemiBold)
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag("forgot_password_back_login_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFF49454F), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to Sign In", color = Color(0xFF49454F))
                }
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    authViewModel: AuthViewModel,
    onResetSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Password strength logic
    val (strengthScore, strengthLabel, strengthColor) = remember(newPassword) {
        when {
            newPassword.isEmpty() -> Triple(0f, "Empty", Color.LightGray)
            newPassword.length < 6 -> Triple(0.25f, "Weak", Color(0xFFE53935))
            newPassword.length < 8 -> Triple(0.5f, "Fair", Color(0xFFFB8C00))
            newPassword.any { it.isDigit() } && newPassword.any { !it.isLetterOrDigit() } -> Triple(1.0f, "Strong", Color(0xFF4CAF50))
            else -> Triple(0.75f, "Good", Color(0xFF8E24AA))
        }
    }

    val isPasswordMatching = newPassword.isNotEmpty() && newPassword == confirmPassword

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8FF))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("reset_password_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create New Password", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B1E))
            Text(
                "Enter your single-use reset code and set a strong new password.",
                fontSize = 13.sp,
                color = Color(0xFF49454F),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Token input
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Reset Code / Token") },
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF6750A4)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6750A4),
                    unfocusedBorderColor = Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_password_token_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // New password input
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6750A4)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6750A4),
                    unfocusedBorderColor = Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_password_newpass_input")
            )

            // Password strength indicator
            if (newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth().testTag("reset_password_strength_container")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password Strength", fontSize = 11.sp, color = Color(0xFF49454F))
                        Text(strengthLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = strengthColor)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { strengthScore },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = strengthColor,
                        trackColor = Color(0xFFE7E0EC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm password input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF6750A4)) },
                trailingIcon = {
                    if (confirmPassword.isNotEmpty()) {
                        Icon(
                            imageVector = if (isPasswordMatching) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isPasswordMatching) Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (confirmPassword.isEmpty() || isPasswordMatching) Color(0xFF6750A4) else Color(0xFFE53935),
                    unfocusedBorderColor = Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_password_confirmpass_input")
            )

            // Validation message for non-matching passwords
            if (confirmPassword.isNotEmpty() && !isPasswordMatching) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Passwords do not match.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_password_mismatch_warning")
                )
            }

            // Success banner feedback
            AnimatedVisibility(visible = uiState.infoMessage != null) {
                uiState.infoMessage?.let { info ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("reset_password_success_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF6750A4))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Password Reset Complete", fontWeight = FontWeight.Bold, color = Color(0xFF1D192B), fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = info, color = Color(0xFF49454F), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Error banner feedback
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("reset_password_failure_card")
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = err, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (token.isNotBlank() && newPassword.length >= 8 && isPasswordMatching) {
                        authViewModel.resetPasswordWithToken(token, newPassword)
                    }
                },
                enabled = !uiState.isLoading && token.isNotBlank() && isPasswordMatching && newPassword.length >= 8,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("reset_password_confirm_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Update & Save Password", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
