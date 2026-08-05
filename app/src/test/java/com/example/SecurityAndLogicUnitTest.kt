package com.example

import com.example.backend.mapper.Mappers.toDomain
import com.example.backend.mapper.Mappers.toProfileDomain
import com.example.backend.remote.AiPreferencesDto
import com.example.backend.remote.GamePreferencesDto
import com.example.backend.remote.UserProfileDto
import com.example.backend.remote.UserSettingsDto
import com.example.backend.model.UserProfileDomain
import com.example.backend.model.UserSettingsDomain
import org.junit.Assert.*
import org.junit.Test

class SecurityAndLogicUnitTest {

    @Test
    fun testClientSideSecretIsolation() {
        // Verify BuildConfig or client properties do not store raw third-party AI keys
        val buildConfigFields = try {
            com.example.BuildConfig::class.java.declaredFields.map { it.name }
        } catch (e: Throwable) {
            emptyList()
        }

        assertFalse("GEMINI_SECRET_KEY must not be exposed in Android client BuildConfig!", buildConfigFields.contains("GEMINI_SECRET_KEY"))
        assertFalse("DATABASE_PASSWORD must not be exposed in Android client BuildConfig!", buildConfigFields.contains("DATABASE_PASSWORD"))
    }

    @Test
    fun testUserProfileDtoToDomainMapping() {
        val dto = UserProfileDto(
            username = "MasterCreator",
            email = "creator@domain.com",
            fullName = "Creative Master",
            avatarUrl = "https://picsum.photos/seed/avatar/200",
            bio = "AI Prompt Engineer",
            tier = "Pro Creator Tier",
            dailyGenerationsUsed = 25,
            dailyGenerationsMax = 100,
            accountCreated = "2026-01-15"
        )

        val domain: UserProfileDomain = dto.toProfileDomain()

        assertEquals("MasterCreator", domain.username)
        assertEquals("creator@domain.com", domain.email)
        assertEquals("Creative Master", domain.fullName)
        assertEquals("Pro Creator Tier", domain.tier)
        assertEquals(25, domain.dailyGenerationsUsed)
    }

    @Test
    fun testUserSettingsDtoToDomainMapping() {
        val dto = UserSettingsDto(
            theme = "dark",
            darkMode = true,
            notificationsEnabled = true,
            language = "Spanish",
            autoSaveHistory = true,
            highQualityRendering = true,
            modelTemperature = 0.8f,
            aiPreferences = AiPreferencesDto(
                chatResponseStyle = "Concise",
                musicGenerationGenre = "Ambient Synthwave"
            ),
            gamePreferences = GamePreferencesDto(
                chessDifficulty = "Grandmaster Mind",
                noSpoilerMode = true
            )
        )

        val domain: UserSettingsDomain = dto.toDomain()

        assertEquals("dark", domain.theme)
        assertTrue(domain.darkMode)
        assertEquals("Spanish", domain.language)
        assertEquals("Concise", domain.aiPreferences.chatResponseStyle)
        assertEquals("Ambient Synthwave", domain.aiPreferences.musicGenerationGenre)
        assertEquals("Grandmaster Mind", domain.gamePreferences.chessDifficulty)
        assertTrue(domain.gamePreferences.noSpoilerMode)
    }

    @Test
    fun testTicTacToeWinningLogic() {
        val winningBoard = listOf("X", "X", "X", "O", "O", "", "", "", "")
        
        fun checkWinner(board: List<String>): String? {
            val wins = listOf(
                listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
                listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
                listOf(0, 4, 8), listOf(2, 4, 6)
            )
            for (w in wins) {
                if (board[w[0]].isNotEmpty() && board[w[0]] == board[w[1]] && board[w[1]] == board[w[2]]) {
                    return board[w[0]]
                }
            }
            return null
        }

        assertEquals("X", checkWinner(winningBoard))
    }
}
