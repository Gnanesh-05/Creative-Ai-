package com.example.backend.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "unified_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val moduleType: String, // CHAT, IMAGE, MUSIC, GAME_CHESS, GAME_TICTACTOE, GAME_MAZE
    val title: String,
    val summary: String,
    val timestamp: Long,
    val payloadJson: String
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM unified_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM unified_history WHERE moduleType = :type ORDER BY timestamp DESC")
    fun getHistoryByType(type: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM unified_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM unified_history")
    suspend fun clearAll()
}

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class CreativeAiDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
