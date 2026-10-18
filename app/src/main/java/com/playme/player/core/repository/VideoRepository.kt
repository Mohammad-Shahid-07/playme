package com.playme.player.core.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.playme.player.core.database.PlayMeDatabase
import com.playme.player.core.database.entity.PlaybackHistoryEntity
import com.playme.player.core.database.entity.VaultItemEntity
import com.playme.player.core.model.VideoFolder
import com.playme.player.core.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class VideoSortOrder {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    DURATION_DESC
}

class VideoRepository(
    private val context: Context,
    private val database: PlayMeDatabase
) {
    private val contentResolver = context.contentResolver
    private val historyDao = database.historyDao()
    private val vaultDao = database.vaultDao()

    suspend fun getAllVideos(sortOrder: VideoSortOrder = VideoSortOrder.DATE_DESC): List<VideoItem> =
        withContext(Dispatchers.IO) {
            val videoList = mutableListOf<VideoItem>()

            // Retrieve history once so we can associate each video with its playback progress
            val historyMap = try {
                historyDao.getAllHistoryList().associateBy { it.mediaUri }
            } catch (e: Exception) {
                emptyMap()
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                } else {
                    MediaStore.Video.Media.DATA
                }
            )

            val orderClause = when (sortOrder) {
                VideoSortOrder.DATE_DESC -> "${MediaStore.Video.Media.DATE_ADDED} DESC"
                VideoSortOrder.DATE_ASC -> "${MediaStore.Video.Media.DATE_ADDED} ASC"
                VideoSortOrder.NAME_ASC -> "${MediaStore.Video.Media.DISPLAY_NAME} COLLATE NOCASE ASC"
                VideoSortOrder.NAME_DESC -> "${MediaStore.Video.Media.DISPLAY_NAME} COLLATE NOCASE DESC"
                VideoSortOrder.SIZE_DESC -> "${MediaStore.Video.Media.SIZE} DESC"
                VideoSortOrder.DURATION_DESC -> "${MediaStore.Video.Media.DURATION} DESC"
            }

            contentResolver.query(
                collection,
                projection,
                null,
                null,
                orderClause
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Video $id"
                    val name = cursor.getString(nameCol) ?: title
                    val path = cursor.getString(dataCol) ?: ""
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val dateAdded = cursor.getLong(dateCol)
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)

                    val file = File(path)
                    val folderName = file.parentFile?.name ?: "Internal Storage"
                    val folderPath = file.parentFile?.absolutePath ?: ""

                    val contentUri = ContentUris.withAppendedId(collection, id).toString()
                    val history = historyMap[contentUri] ?: historyMap[path]
                    val lastPosition = history?.lastPositionMs ?: 0L
                    val lastWatched = history?.lastWatchedTimestamp ?: 0L

                    videoList.add(
                        VideoItem(
                            id = id,
                            title = title,
                            displayName = name,
                            path = path,
                            contentUri = contentUri,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            width = width,
                            height = height,
                            folderName = folderName,
                            folderPath = folderPath,
                            lastPlayedPositionMs = lastPosition,
                            lastWatchedTimestamp = lastWatched
                        )
                    )
                }
            }

            videoList
        }

    suspend fun getLastPlayedOverall(): VideoItem? = withContext(Dispatchers.IO) {
        val lastHistory = historyDao.getLastPlayedVideo() ?: return@withContext null
        getAllVideos().firstOrNull { it.contentUri == lastHistory.mediaUri || it.path == lastHistory.mediaUri }
    }

    suspend fun getLastPlayedInFolder(folderPath: String): VideoItem? = withContext(Dispatchers.IO) {
        getVideosInFolder(folderPath)
            .filter { it.lastWatchedTimestamp > 0L || it.lastPlayedPositionMs > 0L }
            .maxByOrNull { it.lastWatchedTimestamp }
    }

    suspend fun getVideoFolders(): List<VideoFolder> = withContext(Dispatchers.IO) {
        val videos = getAllVideos()
        val folderMap = mutableMapOf<String, MutableList<VideoItem>>()

        for (v in videos) {
            folderMap.getOrPut(v.folderPath) { mutableListOf() }.add(v)
        }

        folderMap.map { (path, list) ->
            val first = list.firstOrNull()
            VideoFolder(
                id = path,
                name = first?.folderName ?: "Folder",
                path = path,
                videoCount = list.size,
                thumbnailUri = first?.contentUri
            )
        }.sortedByDescending { it.videoCount }
    }

    suspend fun getVideosInFolder(folderPath: String): List<VideoItem> = withContext(Dispatchers.IO) {
        getAllVideos().filter { it.folderPath == folderPath }
    }

    suspend fun searchVideos(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val lower = query.lowercase()
        getAllVideos().filter {
            it.displayName.lowercase().contains(lower) ||
            it.folderName.lowercase().contains(lower)
        }
    }

    // Playback Progress & History
    suspend fun saveProgress(uri: String, title: String, positionMs: Long, durationMs: Long) {
        historyDao.saveProgress(
            PlaybackHistoryEntity(
                mediaUri = uri,
                title = title,
                lastPositionMs = positionMs,
                durationMs = durationMs,
                lastWatchedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getLastPosition(uri: String): Long {
        return historyDao.getProgressForUri(uri)?.lastPositionMs ?: 0L
    }

    fun getHistoryFlow(): Flow<List<PlaybackHistoryEntity>> = historyDao.getAllHistory()

    // Private Vault (Encryption / Hidden Storage)
    suspend fun moveToVault(video: VideoItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val vaultDir = File(context.filesDir, "vault_videos").apply { if (!exists()) mkdirs() }
            val vaultFile = File(vaultDir, "vault_${video.id}_${System.currentTimeMillis()}.bin")

            val sourceFile = File(video.path)
            if (!sourceFile.exists()) return@withContext false

            // XOR obfuscate/encrypt bytes on copy
            val key = 0x5A.toByte()
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(vaultFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        for (i in 0 until bytesRead) {
                            buffer[i] = (buffer[i].toInt() xor key.toInt()).toByte()
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            vaultDao.insertVaultItem(
                VaultItemEntity(
                    originalPath = video.path,
                    vaultPath = vaultFile.absolutePath,
                    title = video.displayName,
                    durationMs = video.durationMs,
                    sizeBytes = video.sizeBytes,
                    addedTimestamp = System.currentTimeMillis()
                )
            )

            // Try deleting the original from public storage (direct file delete, then MediaStore fallback)
            val deleted = sourceFile.delete()
            if (!deleted && video.contentUri.isNotBlank()) {
                try {
                    contentResolver.delete(Uri.parse(video.contentUri), null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getVaultItemsFlow(): Flow<List<VaultItemEntity>> = vaultDao.getAllVaultItems()
}
