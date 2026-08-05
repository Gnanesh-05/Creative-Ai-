package com.example.frontend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend.repository.CreativeAiRepository
import com.example.backend.model.AiPreferencesDomain
import com.example.backend.model.GamePreferencesDomain
import com.example.backend.model.UserProfileDomain
import com.example.backend.model.UserSettingsDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfileDomain = UserProfileDomain(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

data class SettingsUiState(
    val settings: UserSettingsDomain = UserSettingsDomain(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isPasswordChanged: Boolean = false,
    val isAccountDeleted: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    private val _settingsUiState = MutableStateFlow(SettingsUiState())
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState.asStateFlow()

    init {
        loadProfileAndSettings()
    }

    fun loadProfileAndSettings() {
        viewModelScope.launch {
            _profileUiState.value = _profileUiState.value.copy(isLoading = true)
            _settingsUiState.value = _settingsUiState.value.copy(isLoading = true)

            val profileRes = repository.getUserProfile()
            profileRes.onSuccess { domain ->
                _profileUiState.value = _profileUiState.value.copy(isLoading = false, profile = domain)
            }.onFailure { err ->
                _profileUiState.value = _profileUiState.value.copy(isLoading = false, errorMessage = err.message)
            }

            val settingsRes = repository.getUserSettings()
            settingsRes.onSuccess { domain ->
                _settingsUiState.value = _settingsUiState.value.copy(isLoading = false, settings = domain)
            }.onFailure { err ->
                _settingsUiState.value = _settingsUiState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun updateProfile(fullName: String, username: String, bio: String) {
        viewModelScope.launch {
            _profileUiState.value = _profileUiState.value.copy(isLoading = true, successMessage = null, errorMessage = null)
            val res = repository.updateUserProfile(fullName = fullName, username = username, bio = bio)
            res.onSuccess { updated ->
                _profileUiState.value = _profileUiState.value.copy(
                    isLoading = false,
                    isEditing = false,
                    profile = updated,
                    successMessage = "Profile updated successfully!"
                )
            }.onFailure { err ->
                _profileUiState.value = _profileUiState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun toggleEditProfile(editing: Boolean) {
        _profileUiState.value = _profileUiState.value.copy(isEditing = editing)
    }

    fun updateTheme(newTheme: String) {
        val current = _settingsUiState.value.settings
        val isDark = newTheme != "light"
        val updated = current.copy(theme = newTheme, darkMode = isDark)
        _settingsUiState.value = _settingsUiState.value.copy(settings = updated)
        saveSettingsToServer(theme = newTheme, darkMode = isDark)
    }

    fun updateNotifications(enabled: Boolean) {
        val current = _settingsUiState.value.settings
        val updated = current.copy(notificationsEnabled = enabled)
        _settingsUiState.value = _settingsUiState.value.copy(settings = updated)
        saveSettingsToServer(notificationsEnabled = enabled)
    }

    fun updateLanguage(lang: String) {
        val current = _settingsUiState.value.settings
        val updated = current.copy(language = lang)
        _settingsUiState.value = _settingsUiState.value.copy(settings = updated)
        saveSettingsToServer(language = lang)
    }

    fun updateAiPreferences(
        chatStyle: String? = null,
        imageModel: String? = null,
        imageAspect: String? = null,
        musicGenre: String? = null,
        filterLevel: String? = null
    ) {
        val currentSettings = _settingsUiState.value.settings
        val currentAi = currentSettings.aiPreferences
        val newAi = currentAi.copy(
            chatResponseStyle = chatStyle ?: currentAi.chatResponseStyle,
            imageGenerationModel = imageModel ?: currentAi.imageGenerationModel,
            imageAspectRatio = imageAspect ?: currentAi.imageAspectRatio,
            musicGenerationGenre = musicGenre ?: currentAi.musicGenerationGenre,
            contentFilterLevel = filterLevel ?: currentAi.contentFilterLevel
        )
        val updatedSettings = currentSettings.copy(aiPreferences = newAi)
        _settingsUiState.value = _settingsUiState.value.copy(settings = updatedSettings)
        saveSettingsToServer(aiPreferences = newAi)
    }

    fun updateGamePreferences(
        chessDiff: String? = null,
        tictactoeDiff: String? = null,
        mazeSz: Int? = null,
        coaching: Boolean? = null,
        sound: Boolean? = null,
        noSpoiler: Boolean? = null
    ) {
        val currentSettings = _settingsUiState.value.settings
        val currentGame = currentSettings.gamePreferences
        val newGame = currentGame.copy(
            chessDifficulty = chessDiff ?: currentGame.chessDifficulty,
            tictactoeDifficulty = tictactoeDiff ?: currentGame.tictactoeDifficulty,
            mazeSize = mazeSz ?: currentGame.mazeSize,
            aiCoachingEnabled = coaching ?: currentGame.aiCoachingEnabled,
            soundEffectsEnabled = sound ?: currentGame.soundEffectsEnabled,
            noSpoilerMode = noSpoiler ?: currentGame.noSpoilerMode
        )
        val updatedSettings = currentSettings.copy(gamePreferences = newGame)
        _settingsUiState.value = _settingsUiState.value.copy(settings = updatedSettings)
        saveSettingsToServer(gamePreferences = newGame)
    }

    private fun saveSettingsToServer(
        theme: String? = null,
        darkMode: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        language: String? = null,
        aiPreferences: AiPreferencesDomain? = null,
        gamePreferences: GamePreferencesDomain? = null
    ) {
        viewModelScope.launch {
            repository.updateUserSettings(
                theme = theme,
                darkMode = darkMode,
                notificationsEnabled = notificationsEnabled,
                language = language,
                aiPreferences = aiPreferences,
                gamePreferences = gamePreferences
            )
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            _settingsUiState.value = _settingsUiState.value.copy(isLoading = true, successMessage = null, errorMessage = null)
            val res = repository.changePassword(currentPass, newPass)
            res.onSuccess {
                _settingsUiState.value = _settingsUiState.value.copy(
                    isLoading = false,
                    isPasswordChanged = true,
                    successMessage = "Password changed successfully!"
                )
            }.onFailure { err ->
                _settingsUiState.value = _settingsUiState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun deleteAccount(passwordConfirm: String) {
        viewModelScope.launch {
            _settingsUiState.value = _settingsUiState.value.copy(isLoading = true, errorMessage = null)
            val res = repository.deleteAccount(passwordConfirm)
            res.onSuccess {
                _settingsUiState.value = _settingsUiState.value.copy(isLoading = false, isAccountDeleted = true)
            }.onFailure { err ->
                _settingsUiState.value = _settingsUiState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun clearMessages() {
        _profileUiState.value = _profileUiState.value.copy(successMessage = null, errorMessage = null)
        _settingsUiState.value = _settingsUiState.value.copy(successMessage = null, errorMessage = null)
    }
}
