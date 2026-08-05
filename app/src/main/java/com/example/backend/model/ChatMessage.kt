package com.example.backend.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long = 1,
    val sender: String, // "USER" or "ASSISTANT" or "AGENT"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String = "gemini-3.5-flash",
    val agentName: String? = null,
    val thinkingSteps: String? = null, // JSON or formatted bullet steps
    val imageUri: String? = null,
    val codeBlock: String? = null,
    val citations: String? = null,
    val emotion: String = "Neutral",
    val tokensCount: Int = 0,
    val isStreaming: Boolean = false
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val modelId: String = "gemini-3.5-flash",
    val activeAgent: String = "PLANNER"
)
