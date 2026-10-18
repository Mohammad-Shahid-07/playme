package com.playme.player.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val mediaUri: String,
    val title: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val lastWatchedTimestamp: Long
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val originalPath: String,
    val vaultPath: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val addedTimestamp: Long
)
