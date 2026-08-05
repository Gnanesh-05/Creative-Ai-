package com.example.backend.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.backend.model.ChatMessage
import com.example.backend.model.ChatSession
import com.example.backend.model.MemoryFact
import com.example.backend.model.StudioAsset
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearSessionMessages(sessionId: Long)

    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdated DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): ChatSession?
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_facts ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMemoryFacts(): Flow<List<MemoryFact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: MemoryFact): Long

    @Query("DELETE FROM memory_facts WHERE id = :id")
    suspend fun deleteFact(id: Long)

    @Query("UPDATE memory_facts SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)
}

@Dao
interface StudioDao {
    @Query("SELECT * FROM studio_assets ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<StudioAsset>>

    @Query("SELECT * FROM studio_assets WHERE type = :type ORDER BY createdAt DESC")
    fun getAssetsByType(type: String): Flow<List<StudioAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: StudioAsset): Long

    @Query("DELETE FROM studio_assets WHERE id = :id")
    suspend fun deleteAsset(id: Long)
}
