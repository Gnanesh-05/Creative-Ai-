package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CreativeAiRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SplashState {
    object Loading : SplashState()
    data class Navigate(val route: String) : SplashState()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkStartupRouting()
    }

    fun checkStartupRouting() {
        viewModelScope.launch {
            _state.value = SplashState.Loading
            // Small animation delay for smooth branding splash display
            delay(1800)

            val isOnboardingCompleted = repository.isOnboardingCompleted()
            val isLoggedIn = repository.isLoggedIn()

            val targetRoute = when {
                !isOnboardingCompleted -> Screen.Onboarding.route
                !isLoggedIn -> Screen.Login.route
                else -> Screen.Home.route
            }

            _state.value = SplashState.Navigate(targetRoute)
        }
    }
}
