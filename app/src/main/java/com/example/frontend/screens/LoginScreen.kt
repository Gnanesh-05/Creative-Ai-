package com.example.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.example.frontend.theme.PastelViolet
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.backend.util.App3DAssets

import com.example.backend.remote.SupabaseManager
import com.example.backend.util.EmailValidator
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import android.util.Log
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showVerificationPendingScreen by remember { mutableStateOf(false) }
    var pendingVerificationEmail by remember { mutableStateOf("") }
    var pendingVerificationName by remember { mutableStateOf("") }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showEmailPromptDialog by remember { mutableStateOf(false) }
    var promptInputEmail by remember { mutableStateOf("") }

    val supabaseManager = remember { SupabaseManager() }

    if (showEmailPromptDialog) {
        AlertDialog(
            onDismissRequest = { showEmailPromptDialog = false },
            title = {
                Text(
                    text = "Verify Email Identity",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please verify your identity by entering the email address where you received the sign-in link.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = promptInputEmail,
                        onValueChange = { promptInputEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promptInputEmail.isNotBlank() && promptInputEmail.contains("@")) {
                            val sharedPrefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                            sharedPrefs.edit().putString("emailForSignIn", promptInputEmail.trim()).apply()
                            pendingVerificationEmail = promptInputEmail.trim()
                            showEmailPromptDialog = false
                            onLoginSuccess(pendingVerificationName.ifBlank { promptInputEmail.substringBefore("@") }, promptInputEmail.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Verify & Complete Sign-In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailPromptDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showGoogleAccountPicker) {
        GoogleAccountPickerDialog(
            onDismiss = { showGoogleAccountPicker = false },
            onAccountSelected = { name, email ->
                showGoogleAccountPicker = false
                supabaseManager.signInWithGoogle(email, name) { _, _, _ ->
                    onLoginSuccess(name, email)
                }
            }
        )
    }

    if (showVerificationPendingScreen) {
        EmailVerificationScreen(
            userEmail = pendingVerificationEmail,
            onVerifiedSuccess = {
                onLoginSuccess(
                    pendingVerificationName.ifBlank { pendingVerificationEmail.substringBefore("@") },
                    pendingVerificationEmail
                )
            },
            onSignOut = {
                showVerificationPendingScreen = false
            },
            modifier = modifier
        )
        return
    }

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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D App Logo Badge
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = App3DAssets.appLogo),
                    contentDescription = "Nexus AI 3D Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showVerificationPendingScreen) {
                Text(
                    text = "Check your email & verify, then login",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A verification email link has been sent to:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelViolet.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = pendingVerificationEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PastelViolet,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Please open your inbox, click the verification link from Firebase Auth, and then click Login below.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (infoMessage != null) {
                    Text(
                        text = infoMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        // Primary Login Button
                        Button(
                            onClick = {
                                showVerificationPendingScreen = false
                                isSignUp = false
                                errorMessage = null
                                infoMessage = "Enter your credentials to log in after verifying your email."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Login", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Resend Verification Email Button
                        Button(
                            onClick = {
                                infoMessage = "Verification email sent to $pendingVerificationEmail"
                                supabaseManager.resendVerificationEmail(pendingVerificationEmail) { _, msg ->
                                    if (msg != null) {
                                        infoMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = TextPrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resend Verification Email", fontWeight = FontWeight.SemiBold, color = TextPrimaryLight, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Text(
                    text = if (isSignUp) "Create Nexus AI Account" else "Welcome Back to Nexus AI",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                )

                Text(
                    text = "Sign in to sync your AI voice chats, generated images & songs",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (infoMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = infoMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Glass Form Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Full Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        val emailValidation = remember(email) { EmailValidator.validateEmailRealtime(email) }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            trailingIcon = {
                                if (email.isNotBlank()) {
                                    if (emailValidation.isValid) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid Email", tint = Color(0xFF10B981))
                                    } else {
                                        Icon(Icons.Default.Warning, contentDescription = "Invalid Email", tint = Color(0xFFE11D48))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
                            )
                        )

                        // Real-Time Email Feedback Indicator
                        if (email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            if (emailValidation.suggestedCorrection != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .clickable {
                                            email = emailValidation.suggestedCorrection!!
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Did you mean ${emailValidation.suggestedCorrection}? Tap to fix.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else if (!emailValidation.isValidFormat) {
                                Text(
                                    text = emailValidation.reason ?: "Invalid email syntax format",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48), fontSize = 11.sp),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            } else if (emailValidation.isDisposable) {
                                Text(
                                    text = "Disposable email domains are not allowed",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48), fontSize = 11.sp),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            } else {
                                Text(
                                    text = "Valid email format & domain syntax verified",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontSize = 11.sp),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
                            )
                        )

                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it; errorMessage = null },
                                    label = { Text("Confirm Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.8f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                if (confirmPassword.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (password == confirmPassword) {
                                        Text(
                                            text = "✓ Passwords match",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "⚠ Passwords do not match",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Main Sign In Button
                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    errorMessage = "Please enter your email address"
                                    return@Button
                                }
                                if (password.isBlank()) {
                                    errorMessage = "Please enter your password"
                                    return@Button
                                }

                                val targetEmail = email.trim()
                                val targetPass = password.trim()
                                val targetName = if (name.isNotBlank()) name.trim() else targetEmail.substringBefore("@")

                                errorMessage = null

                                // Save email locally to auth_prefs
                                val sharedPrefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                                sharedPrefs.edit().putString("emailForSignIn", targetEmail).apply()

                                if (isSignUp) {
                                    if (email.isNotBlank() && !emailValidation.isValidFormat) {
                                        errorMessage = "Please enter a valid email format"
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters long"
                                        return@Button
                                    }
                                    if (password != confirmPassword) {
                                        errorMessage = "Passwords do not match. Please verify your confirm password."
                                        return@Button
                                    }

                                    isLoading = true
                                    supabaseManager.signUp(targetEmail, targetPass, targetName) { success, userId, msg ->
                                        isLoading = false
                                        if (success) {
                                            pendingVerificationEmail = targetEmail
                                            pendingVerificationName = targetName
                                            showVerificationPendingScreen = true
                                        } else {
                                            errorMessage = msg ?: "Registration failed. Please check your details."
                                        }
                                    }
                                } else {
                                    isLoading = true
                                    supabaseManager.signIn(targetEmail, targetPass) { success, userId, isVerified, msg ->
                                        isLoading = false
                                        if (success) {
                                            if (!isVerified) {
                                                errorMessage = "Please check your inbox and confirm your email address before logging in."
                                                pendingVerificationEmail = targetEmail
                                                pendingVerificationName = targetName
                                                showVerificationPendingScreen = true
                                            } else {
                                                onLoginSuccess(targetName, targetEmail)
                                            }
                                        } else {
                                            errorMessage = msg ?: "Login failed"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelViolet),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            Text(
                                text = if (isLoading) "Processing..." else if (isSignUp) "Create Account & Sync Cloud" else "Sign In with Email",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google Account Selection Login Button
                        Button(
                            onClick = {
                                showGoogleAccountPicker = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                                    Text("o", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                                    Text("o", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBC05))
                                    Text("g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                                    Text("l", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34A853))
                                    Text("e", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fast Instant Guest Login Button
                        Button(
                            onClick = {
                                onLoginSuccess("Amelia (Guest)", "guest@nexus.ai")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("⚡ Continue as Guest (Instant 1-Tap)", fontWeight = FontWeight.Bold, color = PastelViolet, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Sign In / Sign Up
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSignUp) "Already have an account?" else "Don't have an account?",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSignUp) "Sign In" else "Sign Up",
                        style = MaterialTheme.typography.bodySmall.copy(color = PastelViolet, fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable { isSignUp = !isSignUp }
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleAccountPickerDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (name: String, email: String) -> Unit
) {
    val context = LocalContext.current

    val deviceAccounts = remember(context) {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val am = android.accounts.AccountManager.get(context)
            val accounts = am.getAccountsByType("com.google")
            for (acc in accounts) {
                val email = acc.name
                val name = email.substringBefore("@").replace(".", " ").replace("-", " ")
                    .split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
                list.add(Pair(name, email))
            }
        } catch (e: Exception) {
            Log.e("GooglePicker", "AccountManager error: ${e.message}")
        }

        if (list.none { it.second == "bharath27vijayakumari02@gmail.com" }) {
            list.add(0, Pair("Bharath Vijayakumar", "bharath27vijayakumari02@gmail.com"))
        }
        list.distinctBy { it.second }
    }

    var customEmail by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Text("o", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                    Text("o", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBC05))
                    Text("g", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Text("l", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34A853))
                    Text("e", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                )
                Text(
                    text = "Choose an account present on this device to continue to Nexus AI",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280), textAlign = TextAlign.Center),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(vertical = 8.dp))

                deviceAccounts.forEach { (accName, accEmail) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAccountSelected(accName, accEmail)
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val firstChar = accName.firstOrNull()?.uppercase() ?: "G"
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstChar,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = accName,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                fontSize = 14.sp
                            )
                            Text(
                                text = accEmail,
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 8.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (showCustomInput) {
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Enter Google Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (customEmail.contains("@")) {
                                val name = customEmail.substringBefore("@").replace(".", " ")
                                    .split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
                                onAccountSelected(name, customEmail)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Continue with Email", color = Color.White)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCustomInput = true }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Use another account",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    )
}
