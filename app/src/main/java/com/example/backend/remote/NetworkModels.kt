package com.example.backend.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Generic API Wrapper matching FastAPI StandardResponse
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "data") val data: T? = null,
    @Json(name = "message") val message: String? = null
)

// --- Domain Models / Legacy Wrappers used by ViewModels ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val fullName: String = ""
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val userId: String,
    val username: String,
    val email: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String
)

data class ChatRequest(
    val message: String,
    val model: String = "gemini-2.0-flash",
    val systemInstruction: String? = null
)

data class ChatResponse(
    val reply: String,
    val tokensUsed: Int = 120,
    val sessionId: String
)

data class ImageGenerateRequest(
    val prompt: String,
    val stylePreset: String = "Realistic",
    val aspectRatio: String = "1:1"
)

data class ImageGenerateResponse(
    val id: String,
    val prompt: String,
    val imageUrl: String,
    val createdAt: String
)

data class MusicComposeRequest(
    val prompt: String,
    val genre: String = "Cinematic Ambient",
    val tempoBpm: Int = 120,
    val durationSeconds: Int = 30
)

data class MusicComposeResponse(
    val id: String,
    val prompt: String,
    val genre: String,
    val audioUrl: String,
    val notesSequence: List<Int>
)

data class ChessMoveRequest(
    val fen: String,
    val playerMove: String,
    val difficulty: String = "MEDIUM"
)

data class ChessMoveResponse(
    val aiMove: String,
    val newFen: String,
    val evaluationScore: Float,
    val isCheckmate: Boolean,
    val isDraw: Boolean
)

data class TicTacToeMoveRequest(
    val boardState: List<String>,
    val difficulty: String = "HARD"
)

data class TicTacToeMoveResponse(
    val aiMoveIndex: Int,
    val winner: String?
)

data class MazeGenerateRequest(
    val width: Int = 15,
    val height: Int = 15,
    val difficulty: String = "MEDIUM"
)

data class MazeGenerateResponse(
    val grid: List<List<Int>>,
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int,
    val solutionPath: List<List<Int>>
)

data class UserProfile(
    val userId: String,
    val username: String,
    val email: String,
    val fullName: String,
    val planTier: String = "Pro Member",
    val totalAiRequests: Int = 142
)

// --- Retrofit DTOs matching FastAPI JSON ---
@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class PasswordResetRequestDto(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class PasswordResetConfirmRequestDto(
    @Json(name = "token") val token: String,
    @Json(name = "newPassword") val newPassword: String
)


@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "token") val token: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "full_name") val fullName: String? = "Creative Master",
    @Json(name = "avatar_url") val avatarUrl: String? = "https://picsum.photos/seed/useravatar/200",
    @Json(name = "bio") val bio: String? = "AI Enthusiast & Game Creator",
    @Json(name = "tier") val tier: String = "Pro Creator Tier",
    @Json(name = "dailyGenerationsUsed") val dailyGenerationsUsed: Int = 18,
    @Json(name = "dailyGenerationsMax") val dailyGenerationsMax: Int = 100,
    @Json(name = "accountCreated") val accountCreated: String = "2026-01-15"
)

@JsonClass(generateAdapter = true)
data class UserProfileUpdateDto(
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "bio") val bio: String? = null
)

@JsonClass(generateAdapter = true)
data class AiPreferencesDto(
    @Json(name = "chat_response_style") val chatResponseStyle: String = "Detailed & Creative",
    @Json(name = "image_generation_model") val imageGenerationModel: String = "imagen-3.0-generate-002",
    @Json(name = "image_aspect_ratio") val imageAspectRatio: String = "1:1",
    @Json(name = "music_generation_genre") val musicGenerationGenre: String = "Ambient Synthwave",
    @Json(name = "content_filter_level") val contentFilterLevel: String = "Standard"
)

@JsonClass(generateAdapter = true)
data class GamePreferencesDto(
    @Json(name = "chess_difficulty") val chessDifficulty: String = "Grandmaster Mind",
    @Json(name = "tictactoe_difficulty") val tictactoeDifficulty: String = "Unbeatable",
    @Json(name = "maze_size") val mazeSize: Int = 15,
    @Json(name = "ai_coaching_enabled") val aiCoachingEnabled: Boolean = true,
    @Json(name = "sound_effects_enabled") val soundEffectsEnabled: Boolean = true,
    @Json(name = "no_spoiler_mode") val noSpoilerMode: Boolean = false
)

@JsonClass(generateAdapter = true)
data class UserSettingsDto(
    @Json(name = "theme") val theme: String = "system",
    @Json(name = "darkMode") val darkMode: Boolean = true,
    @Json(name = "notificationsEnabled") val notificationsEnabled: Boolean = true,
    @Json(name = "language") val language: String = "English",
    @Json(name = "autoSaveHistory") val autoSaveHistory: Boolean = true,
    @Json(name = "highQualityRendering") val highQualityRendering: Boolean = true,
    @Json(name = "modelTemperature") val modelTemperature: Float = 0.7f,
    @Json(name = "ai_preferences") val aiPreferences: AiPreferencesDto = AiPreferencesDto(),
    @Json(name = "game_preferences") val gamePreferences: GamePreferencesDto = GamePreferencesDto()
)

@JsonClass(generateAdapter = true)
data class UserSettingsUpdateDto(
    @Json(name = "theme") val theme: String? = null,
    @Json(name = "darkMode") val darkMode: Boolean? = null,
    @Json(name = "notificationsEnabled") val notificationsEnabled: Boolean? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "autoSaveHistory") val autoSaveHistory: Boolean? = null,
    @Json(name = "highQualityRendering") val highQualityRendering: Boolean? = null,
    @Json(name = "modelTemperature") val modelTemperature: Float? = null,
    @Json(name = "ai_preferences") val aiPreferences: AiPreferencesDto? = null,
    @Json(name = "game_preferences") val gamePreferences: GamePreferencesDto? = null
)

@JsonClass(generateAdapter = true)
data class ChangePasswordDto(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class DeleteAccountDto(
    @Json(name = "password_confirmation") val passwordConfirmation: String
)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "system_instruction") val systemInstruction: String? = null,
    @Json(name = "model_name") val modelName: String = "gemini-2.0-flash",
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = "",
    @Json(name = "last_message_snippet") val lastMessageSnippet: String? = null,
    @Json(name = "message_count") val messageCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ChatMessageItemDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "conversation_id") val conversationId: String? = null,
    @Json(name = "sender") val sender: String,
    @Json(name = "content") val content: String,
    @Json(name = "tokens_used") val tokensUsed: Int? = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ConversationDetailDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "system_instruction") val systemInstruction: String? = null,
    @Json(name = "model_name") val modelName: String = "gemini-2.0-flash",
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = "",
    @Json(name = "last_message_snippet") val lastMessageSnippet: String? = null,
    @Json(name = "message_count") val messageCount: Int = 0,
    @Json(name = "messages") val messages: List<ChatMessageItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CreateConversationRequestDto(
    @Json(name = "title") val title: String = "New Conversation",
    @Json(name = "system_instruction") val systemInstruction: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateConversationRequestDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "system_instruction") val systemInstruction: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatRequestDto(
    @Json(name = "message") val message: String,
    @Json(name = "history") val history: List<ChatMessageDto>? = emptyList(),
    @Json(name = "temperature") val temperature: Float = 0.7f
)

@JsonClass(generateAdapter = true)
data class ChatResponseDto(
    @Json(name = "reply") val reply: String,
    @Json(name = "model") val model: String = "gemini-2.0-flash"
)

@JsonClass(generateAdapter = true)
data class ImageGenRequestDto(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "enhancedPrompt") val enhancedPrompt: String? = null,
    @Json(name = "negativePrompt") val negativePrompt: String? = null,
    @Json(name = "aspectRatio") val aspectRatio: String = "1:1",
    @Json(name = "stylePreset") val stylePreset: String = "Photorealistic",
    @Json(name = "resolution") val resolution: String = "1024x1024",
    @Json(name = "model") val model: String = "imagen-3.0-generate-002",
    @Json(name = "numImages") val numImages: Int = 1
)

@JsonClass(generateAdapter = true)
data class EnhancePromptRequestDto(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "stylePreset") val stylePreset: String = "Photorealistic"
)

@JsonClass(generateAdapter = true)
data class EnhancePromptResponseDto(
    @Json(name = "originalPrompt") val originalPrompt: String,
    @Json(name = "enhancedPrompt") val enhancedPrompt: String
)

@JsonClass(generateAdapter = true)
data class ImageGenResponseDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "imageUrl") val imageUrl: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "enhancedPrompt") val enhancedPrompt: String? = null,
    @Json(name = "negativePrompt") val negativePrompt: String? = null,
    @Json(name = "aspectRatio") val aspectRatio: String = "1:1",
    @Json(name = "stylePreset") val stylePreset: String = "Photorealistic",
    @Json(name = "resolution") val resolution: String = "1024x1024",
    @Json(name = "model") val model: String = "imagen-3.0-generate-002",
    @Json(name = "storageReference") val storageReference: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ImageJobResponseDto(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "progress") val progress: Int = 0,
    @Json(name = "errorMessage") val errorMessage: String? = null,
    @Json(name = "results") val results: List<ImageGenResponseDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MusicGenRequestDto(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "enhancedPrompt") val enhancedPrompt: String? = null,
    @Json(name = "genre") val genre: String = "Lo-Fi Beats",
    @Json(name = "mood") val mood: String = "Relaxing",
    @Json(name = "tempoBpm") val tempoBpm: Int = 90,
    @Json(name = "durationSeconds") val durationSeconds: Int = 30,
    @Json(name = "keySignature") val keySignature: String = "C Major",
    @Json(name = "instruments") val instruments: String = "Piano, Strings",
    @Json(name = "energyLevel") val energyLevel: String = "Medium",
    @Json(name = "isInstrumental") val isInstrumental: Boolean = true,
    @Json(name = "lyrics") val lyrics: String? = null,
    @Json(name = "model") val model: String = "musicgen-stereo-large"
)

@JsonClass(generateAdapter = true)
data class EnhanceMusicPromptRequestDto(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "genre") val genre: String = "Lo-Fi Beats",
    @Json(name = "mood") val mood: String = "Relaxing"
)

@JsonClass(generateAdapter = true)
data class EnhanceMusicPromptResponseDto(
    @Json(name = "originalPrompt") val originalPrompt: String,
    @Json(name = "enhancedPrompt") val enhancedPrompt: String
)

@JsonClass(generateAdapter = true)
data class MusicTrackResponseDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "enhancedPrompt") val enhancedPrompt: String? = null,
    @Json(name = "genre") val genre: String = "Lo-Fi Beats",
    @Json(name = "mood") val mood: String = "Relaxing",
    @Json(name = "tempoBpm") val tempoBpm: Int = 90,
    @Json(name = "durationSeconds") val durationSeconds: Int = 30,
    @Json(name = "keySignature") val keySignature: String = "C Major",
    @Json(name = "instruments") val instruments: String = "Piano, Strings",
    @Json(name = "energyLevel") val energyLevel: String = "Medium",
    @Json(name = "isInstrumental") val isInstrumental: Boolean = true,
    @Json(name = "lyrics") val lyrics: String? = null,
    @Json(name = "model") val model: String = "musicgen-stereo-large",
    @Json(name = "audioUrl") val audioUrl: String,
    @Json(name = "audioStorageReference") val audioStorageReference: String? = null,
    @Json(name = "syntheticNotes") val syntheticNotes: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "isSaved") val isSaved: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MusicJobResponseDto(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "progress") val progress: Int = 0,
    @Json(name = "errorMessage") val errorMessage: String? = null,
    @Json(name = "results") val results: List<MusicTrackResponseDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChessMoveRequestDto(
    @Json(name = "moveFrom") val moveFrom: String,
    @Json(name = "moveTo") val moveTo: String,
    @Json(name = "fen") val fen: String
)

@JsonClass(generateAdapter = true)
data class TicTacToeMoveRequestDto(
    @Json(name = "board") val board: List<String>,
    @Json(name = "difficulty") val difficulty: String = "Unbeatable"
)

@JsonClass(generateAdapter = true)
data class MazeRequestDto(
    @Json(name = "rows") val rows: Int = 15,
    @Json(name = "cols") val cols: Int = 15
)

@JsonClass(generateAdapter = true)
data class GameResponseDto(
    @Json(name = "status") val status: String,
    @Json(name = "boardState") val boardState: Any? = null,
    @Json(name = "aiMove") val aiMove: String? = null,
    @Json(name = "winner") val winner: String? = null
)

@JsonClass(generateAdapter = true)
data class HistoryItemCreateDto(
    @Json(name = "module_type") val moduleType: String,
    @Json(name = "title") val title: String,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "payload") val payload: Any? = null
)

@JsonClass(generateAdapter = true)
data class HistoryItemReadDto(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "module_type") val moduleType: String,
    @Json(name = "title") val title: String,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "payload") val payload: Any? = null,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class HistoryListDto(
    @Json(name = "items") val items: List<HistoryItemReadDto> = emptyList(),
    @Json(name = "total") val total: Int = 0,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 20,
    @Json(name = "has_more") val hasMore: Boolean = false
)
