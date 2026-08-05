package com.example.backend.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_facts")
data class MemoryFact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Preference", "Project", "Personal", "Fact", "Relationship"
    val fact: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "studio_assets")
data class StudioAsset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "IMAGE", "MUSIC", "VIDEO", "DOCUMENT"
    val title: String,
    val prompt: String,
    val paramsJson: String,
    val assetUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
