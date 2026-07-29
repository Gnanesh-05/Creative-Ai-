package com.example.domain.model

data class UserDomain(
    val userId: String,
    val username: String,
    val email: String,
    val authToken: String = "",
    val tier: String = "Pro Member",
    val dailyGenerationsUsed: Int = 0
)

data class UserProfileDomain(
    val username: String = "Creative User",
    val email: String = "user@creativeai.app",
    val fullName: String = "Creative Master",
    val avatarUrl: String = "https://picsum.photos/seed/useravatar/200",
    val bio: String = "AI Enthusiast & Game Creator",
    val tier: String = "Pro Creator Tier",
    val dailyGenerationsUsed: Int = 18,
    val dailyGenerationsMax: Int = 100,
    val accountCreated: String = "2026-01-15"
)

data class AiPreferencesDomain(
    val chatResponseStyle: String = "Detailed & Creative",
    val imageGenerationModel: String = "imagen-3.0-generate-002",
    val imageAspectRatio: String = "1:1",
    val musicGenerationGenre: String = "Ambient Synthwave",
    val contentFilterLevel: String = "Standard"
)

data class GamePreferencesDomain(
    val chessDifficulty: String = "Grandmaster Mind",
    val tictactoeDifficulty: String = "Unbeatable",
    val mazeSize: Int = 15,
    val aiCoachingEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val noSpoilerMode: Boolean = false
)

data class UserSettingsDomain(
    val theme: String = "system",
    val darkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val language: String = "English",
    val autoSaveHistory: Boolean = true,
    val highQualityRendering: Boolean = true,
    val modelTemperature: Float = 0.7f,
    val aiPreferences: AiPreferencesDomain = AiPreferencesDomain(),
    val gamePreferences: GamePreferencesDomain = GamePreferencesDomain()
)

data class ChatMessageDomain(
    val reply: String,
    val model: String = "gemini-2.0-flash"
)

data class ChatMessageItemDomain(
    val id: String = "",
    val conversationId: String = "",
    val sender: String = "USER", // "USER" or "AI"
    val content: String = "",
    val tokensUsed: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatConversationDomain(
    val id: String,
    val title: String,
    val systemInstruction: String? = null,
    val modelName: String = "gemini-2.0-flash",
    val createdAt: String = "",
    val updatedAt: String = "",
    val lastMessageSnippet: String = "",
    val messageCount: Int = 0,
    val messages: List<ChatMessageItemDomain> = emptyList()
)

data class ImageResultDomain(
    val id: String = "",
    val prompt: String,
    val enhancedPrompt: String? = null,
    val negativePrompt: String? = null,
    val imageUrl: String,
    val aspectRatio: String = "1:1",
    val stylePreset: String = "Photorealistic",
    val resolution: String = "1024x1024",
    val model: String = "imagen-3.0-generate-002",
    val storageReference: String? = null,
    val createdAt: String? = null
)

data class ImageJobDomain(
    val jobId: String,
    val status: String,
    val progress: Int = 0,
    val errorMessage: String? = null,
    val results: List<ImageResultDomain> = emptyList()
)

data class EnhancePromptResultDomain(
    val originalPrompt: String,
    val enhancedPrompt: String
)

data class MusicResultDomain(
    val id: String = "",
    val prompt: String,
    val enhancedPrompt: String? = null,
    val genre: String = "Lo-Fi Beats",
    val mood: String = "Relaxing",
    val tempoBpm: Int = 90,
    val durationSeconds: Int = 30,
    val keySignature: String = "C Major",
    val instruments: String = "Piano, Strings",
    val energyLevel: String = "Medium",
    val isInstrumental: Boolean = true,
    val lyrics: String? = null,
    val model: String = "musicgen-stereo-large",
    val audioUrl: String,
    val audioStorageReference: String? = null,
    val syntheticNotes: String? = null,
    val createdAt: String? = null,
    val isSaved: Boolean = false
)

data class MusicJobDomain(
    val jobId: String,
    val status: String,
    val progress: Int = 0,
    val errorMessage: String? = null,
    val results: List<MusicResultDomain> = emptyList()
)

data class EnhanceMusicPromptResultDomain(
    val originalPrompt: String,
    val enhancedPrompt: String
)

data class GameResultDomain(
    val status: String,
    val boardState: Any? = null,
    val aiMove: String? = null,
    val winner: String? = null
)

data class HistoryItemDomain(
    val id: String,
    val moduleType: String,
    val title: String,
    val summary: String,
    val timestamp: Long,
    val payloadJson: String = ""
)
