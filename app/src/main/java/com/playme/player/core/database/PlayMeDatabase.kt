package com.playme.player.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.playme.player.core.database.dao.PlaybackHistoryDao
import com.playme.player.core.database.dao.VaultDao
import com.playme.player.core.database.entity.PlaybackHistoryEntity
import com.playme.player.core.database.entity.VaultItemEntity

@Database(
    entities = [PlaybackHistoryEntity::class, VaultItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlayMeDatabase : RoomDatabase() {
    abstract fun historyDao(): PlaybackHistoryDao
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: PlayMeDatabase? = null

        fun getDatabase(context: Context): PlayMeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayMeDatabase::class.java,
                    "playme_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
