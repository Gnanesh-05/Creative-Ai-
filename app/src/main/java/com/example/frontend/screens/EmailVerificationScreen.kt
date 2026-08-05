package com.example.frontend.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.remote.SupabaseManager
import com.example.frontend.theme.NexusViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun EmailVerificationScreen(
    onVerifiedSuccess: () -> Unit,
    onSignOut: () -> Unit,
    userEmail: String = "your email",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { SupabaseManager() }

    LaunchedEffect(userEmail) {
        if (userEmail.isNotBlank() && userEmail != "your email") {
            SupabaseManager.currentUserEmail = userEmail
        }
    }

    var isChecking by remember { mutableStateOf(true) }
    var isVerified by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("Checking verification status in real time...") }

    // Polling loop: check verification status via Firebase Auth every 3 seconds
    LaunchedEffect(Unit) {
        while (isActive && !isVerified) {
            authManager.checkEmailVerification { verified ->
                if (verified) {
                    isVerified = true
                    isChecking = false
                    statusMessage = "Email verified successfully!"
                }
            }

            if (isVerified) {
                delay(1000)
                onVerifiedSuccess()
                break
            }

            delay(3000)
        }
    }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown -= 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B1E),
                        Color(0xFF1B1333),
                        Color(0xFF0F0B1E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1735)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVerified) Color(0xFF10B981).copy(alpha = 0.2f)
                            else NexusViolet.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.MarkEmailRead,
                        contentDescription = "Verification Icon",
                        tint = if (isVerified) Color(0xFF10B981) else NexusViolet,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isVerified) "Email Verified!" else "Check your email & verify, then login",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isVerified)
                        "Your email has been verified successfully. Redirecting to app..."
                    else
                        "We've sent a verification link to:\n$userEmail\n\nPlease check your inbox, click the link, and then click Login below.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!isVerified) {
                    // Primary Login Button
                    Button(
                        onClick = {
                            onSignOut()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend Verification Email Button
                    OutlinedButton(
                        onClick = {
                            if (resendCooldown == 0) {
                                authManager.resendVerificationEmail(userEmail) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, "Verification email sent to $userEmail!", Toast.LENGTH_SHORT).show()
                                        resendCooldown = 60
                                    } else {
                                        Toast.makeText(context, msg ?: "Failed to resend email", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        enabled = resendCooldown == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend Verification Email",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Status Indicator
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF281F45)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isChecking && !isVerified) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = NexusViolet,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isVerified) Color(0xFF10B981) else Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
