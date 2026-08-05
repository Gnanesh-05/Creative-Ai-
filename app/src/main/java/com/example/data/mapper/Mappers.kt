package com.example.data.mapper

import com.example.data.local.HistoryEntity
import com.example.data.remote.*
import com.example.domain.model.*

object Mappers {

    fun AuthResponseDto.toDomain(): UserDomain {
        return UserDomain(
            userId = this.userId,
            username = this.username,
            email = this.email,
            authToken = this.token
        )
    }

    fun UserProfileDto.toDomain(authToken: String): UserDomain {
        return UserDomain(
            userId = "",
            username = this.username,
            email = this.email,
            authToken = authToken,
            tier = this.tier,
            dailyGenerationsUsed = this.dailyGenerationsUsed
        )
    }

    fun UserProfileDto.toProfileDomain(): UserProfileDomain {
        return UserProfileDomain(
            username = this.username,
            email = this.email,
            fullName = this.fullName ?: "Creative Master",
            avatarUrl = this.avatarUrl ?: "https://picsum.photos/seed/useravatar/200",
            bio = this.bio ?: "AI Enthusiast & Game Creator",
            tier = this.tier,
            dailyGenerationsUsed = this.dailyGenerationsUsed,
            dailyGenerationsMax = this.dailyGenerationsMax,
            accountCreated = this.accountCreated
        )
    }

    fun AiPreferencesDto.toDomain(): AiPreferencesDomain {
        return AiPreferencesDomain(
            chatResponseStyle = this.chatResponseStyle,
            imageGenerationModel = this.imageGenerationModel,
            imageAspectRatio = this.imageAspectRatio,
            musicGenerationGenre = this.musicGenerationGenre,
            contentFilterLevel = this.contentFilterLevel
        )
    }

    fun GamePreferencesDto.toDomain(): GamePreferencesDomain {
        return GamePreferencesDomain(
            chessDifficulty = this.chessDifficulty,
            tictactoeDifficulty = this.tictactoeDifficulty,
            mazeSize = this.mazeSize,
            aiCoachingEnabled = this.aiCoachingEnabled,
            soundEffectsEnabled = this.soundEffectsEnabled,
            noSpoilerMode = this.noSpoilerMode
        )
    }

    fun UserSettingsDto.toDomain(): UserSettingsDomain {
        return UserSettingsDomain(
            theme = this.theme,
            darkMode = this.darkMode,
            notificationsEnabled = this.notificationsEnabled,
            language = this.language,
            autoSaveHistory = this.autoSaveHistory,
            highQualityRendering = this.highQualityRendering,
            modelTemperature = this.modelTemperature,
            aiPreferences = this.aiPreferences.toDomain(),
            gamePreferences = this.gamePreferences.toDomain()
        )
    }

    fun UserSettingsDomain.toDto(): UserSettingsDto {
        return UserSettingsDto(
            darkMode = this.darkMode,
            notificationsEnabled = this.notificationsEnabled,
            autoSaveHistory = this.autoSaveHistory,
            highQualityRendering = this.highQualityRendering,
            modelTemperature = this.modelTemperature
        )
    }

    fun ChatResponseDto.toDomain(): ChatMessageDomain {
        return ChatMessageDomain(
            reply = this.reply,
            model = this.model
        )
    }

    fun ChatMessageItemDto.toDomain(): ChatMessageItemDomain {
        val s = if (this.sender.equals("user", ignoreCase = true)) "USER" else "AI"
        return ChatMessageItemDomain(
            id = this.id ?: java.util.UUID.randomUUID().toString(),
            conversationId = this.conversationId ?: "",
            sender = s,
            content = this.content,
            tokensUsed = this.tokensUsed ?: 0
        )
    }

    fun ConversationDto.toDomain(): ChatConversationDomain {
        return ChatConversationDomain(
            id = this.id,
            title = this.title,
            systemInstruction = this.systemInstruction,
            modelName = this.modelName,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastMessageSnippet = this.lastMessageSnippet ?: "",
            messageCount = this.messageCount,
            messages = emptyList()
        )
    }

    fun ConversationDetailDto.toDomain(): ChatConversationDomain {
        return ChatConversationDomain(
            id = this.id,
            title = this.title,
            systemInstruction = this.systemInstruction,
            modelName = this.modelName,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            lastMessageSnippet = this.lastMessageSnippet ?: "",
            messageCount = this.messageCount,
            messages = this.messages.map { it.toDomain() }
        )
    }

    fun ImageGenResponseDto.toDomain(): ImageResultDomain {
        return ImageResultDomain(
            id = this.id ?: "",
            prompt = this.prompt,
            enhancedPrompt = this.enhancedPrompt,
            negativePrompt = this.negativePrompt,
            imageUrl = this.imageUrl,
            aspectRatio = this.aspectRatio,
            stylePreset = this.stylePreset,
            resolution = this.resolution,
            model = this.model,
            storageReference = this.storageReference,
            createdAt = this.createdAt
        )
    }

    fun ImageJobResponseDto.toDomain(): ImageJobDomain {
        return ImageJobDomain(
            jobId = this.jobId,
            status = this.status,
            progress = this.progress,
            errorMessage = this.errorMessage,
            results = this.results.map { it.toDomain() }
        )
    }

    fun EnhancePromptResponseDto.toDomain(): EnhancePromptResultDomain {
        return EnhancePromptResultDomain(
            originalPrompt = this.originalPrompt,
            enhancedPrompt = this.enhancedPrompt
        )
    }

    fun MusicTrackResponseDto.toDomain(): MusicResultDomain {
        return MusicResultDomain(
            id = this.id ?: "",
            prompt = this.prompt,
            enhancedPrompt = this.enhancedPrompt,
            genre = this.genre,
            mood = this.mood,
            tempoBpm = this.tempoBpm,
            durationSeconds = this.durationSeconds,
            keySignature = this.keySignature,
            instruments = this.instruments,
            energyLevel = this.energyLevel,
            isInstrumental = this.isInstrumental,
            lyrics = this.lyrics,
            model = this.model,
            audioUrl = this.audioUrl,
            audioStorageReference = this.audioStorageReference,
            syntheticNotes = this.syntheticNotes,
            createdAt = this.createdAt,
            isSaved = this.isSaved
        )
    }

    fun MusicJobResponseDto.toDomain(): MusicJobDomain {
        return MusicJobDomain(
            jobId = this.jobId,
            status = this.status,
            progress = this.progress,
            errorMessage = this.errorMessage,
            results = this.results.map { it.toDomain() }
        )
    }

    fun EnhanceMusicPromptResponseDto.toDomain(): EnhanceMusicPromptResultDomain {
        return EnhanceMusicPromptResultDomain(
            originalPrompt = this.originalPrompt,
            enhancedPrompt = this.enhancedPrompt
        )
    }

    fun GameResponseDto.toDomain(): GameResultDomain {
        return GameResultDomain(
            status = this.status,
            boardState = this.boardState,
            aiMove = this.aiMove,
            winner = this.winner
        )
    }

    fun HistoryItemReadDto.toDomain(): HistoryItemDomain {
        val ts = try {
            System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
        return HistoryItemDomain(
            id = this.id,
            moduleType = this.moduleType,
            title = this.title,
            summary = this.summary ?: "",
            timestamp = ts,
            payloadJson = this.payload?.toString() ?: ""
        )
    }

    fun HistoryItemReadDto.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = this.id,
            moduleType = this.moduleType,
            title = this.title,
            summary = this.summary ?: "",
            timestamp = System.currentTimeMillis(),
            payloadJson = this.payload?.toString() ?: ""
        )
    }

    fun HistoryEntity.toDomain(): HistoryItemDomain {
        return HistoryItemDomain(
            id = this.id,
            moduleType = this.moduleType,
            title = this.title,
            summary = this.summary,
            timestamp = this.timestamp,
            payloadJson = this.payloadJson
        )
    }
}
