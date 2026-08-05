package com.example.frontend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend.remote.LoginRequest
import com.example.backend.remote.RegisterRequest
import com.example.backend.repository.CreativeAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val email: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(LoginRequest(email, pass))
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    username = res.username,
                    email = res.email
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Authentication failed."
                )
            }
        }
    }

    fun register(email: String, uname: String, pass: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(RegisterRequest(email, uname, pass, fullName))
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    username = res.username,
                    email = res.email
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Registration failed."
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your registered email.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.requestPasswordReset(email)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    infoMessage = "If an account with that email exists, reset instructions have been sent.",
                    errorMessage = null
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to send password reset request."
                )
            }
        }
    }

    fun resetPasswordWithToken(token: String, newPass: String) {
        if (token.isBlank() || newPass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Token and new password required.")
            return
        }
        if (newPass.length < 8) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.confirmPasswordReset(token, newPass)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    infoMessage = "Password updated successfully. Please login with your new credentials.",
                    errorMessage = null
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Invalid or expired reset token."
                )
            }
        }
    }


    fun logout() {
        _uiState.value = AuthUiState()
    }
}
