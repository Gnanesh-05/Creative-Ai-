package com.example.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8FF))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("register_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B1E))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Join the Creative AI Multi-Modal Engine", fontSize = 14.sp, color = Color(0xFF49454F))

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6750A4)) },
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
                modifier = Modifier.fillMaxWidth().testTag("register_fullname_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6750A4)) },
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
                modifier = Modifier.fillMaxWidth().testTag("register_username_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6750A4)) },
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
                modifier = Modifier.fillMaxWidth().testTag("register_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6750A4)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                modifier = Modifier.fillMaxWidth().testTag("register_password_input")
            )

            uiState.errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { authViewModel.register(email, username, password, fullName) },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_submit_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Create Account", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already registered?", color = Color(0xFF49454F), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToLogin, modifier = Modifier.testTag("register_login_navigation_button")) {
                    Text("Sign In", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
