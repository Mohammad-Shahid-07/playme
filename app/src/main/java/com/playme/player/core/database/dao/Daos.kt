package com.playme.player.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.playme.player.core.database.entity.PlaybackHistoryEntity
import com.playme.player.core.database.entity.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY lastWatchedTimestamp DESC")
    fun getAllHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY lastWatchedTimestamp DESC")
    suspend fun getAllHistoryList(): List<PlaybackHistoryEntity>

    @Query("SELECT * FROM playback_history ORDER BY lastWatchedTimestamp DESC LIMIT 1")
    suspend fun getLastPlayedVideo(): PlaybackHistoryEntity?

    @Query("SELECT * FROM playback_history WHERE mediaUri = :uri LIMIT 1")
    suspend fun getProgressForUri(uri: String): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(entity: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE mediaUri = :uri")
    suspend fun deleteProgress(uri: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY addedTimestamp DESC")
    fun getAllVaultItems(): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity): Long

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItem(id: Long)
}
