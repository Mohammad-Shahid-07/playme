package com.playme.player.core.scanner

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ThumbnailManager {

    // 25% of max memory for instant in-memory bitmap cache
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 5 // 20% of memory

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getFromMemory(id: String): Bitmap? = memoryCache.get(id)

    suspend fun loadThumbnail(
        context: Context,
        videoId: String,
        uri: Uri,
        filePath: String,
        targetWidth: Int = 360,
        targetHeight: Int = 240
    ): Bitmap? = withContext(Dispatchers.IO) {
        // 1. Check memory cache first
        memoryCache.get(videoId)?.let { return@withContext it }

        // 2. Check disk cache
        val diskCacheFile = File(context.cacheDir, "thumb_$videoId.jpg")
        if (diskCacheFile.exists()) {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeFile(diskCacheFile.absolutePath)
                if (bitmap != null) {
                    memoryCache.put(videoId, bitmap)
                    return@withContext bitmap
                }
            } catch (_: Exception) {}
        }

        // 3. Hardware OS MediaStore Thumbnail (instant <1ms on Android 10+)
        var resultBitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uri.scheme == "content") {
            try {
                resultBitmap = context.contentResolver.loadThumbnail(
                    uri,
                    Size(targetWidth, targetHeight),
                    null
                )
            } catch (_: Exception) {}
        } else if (uri.scheme == "content") {
            try {
                val idLong = videoId.toLongOrNull()
                if (idLong != null) {
                    resultBitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver,
                        idLong,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    )
                }
            } catch (_: Exception) {}
        }

        // 4. Fallback for non-MediaStore files (Telegram/WhatsApp .nomedia)
        if (resultBitmap == null && filePath.isNotEmpty()) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                resultBitmap = retriever.getFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                retriever.release()
            } catch (_: Exception) {}
        }

        // 5. Downsample & cache to disk/memory
        resultBitmap?.let { orig ->
            val scaled = if (orig.width > targetWidth || orig.height > targetHeight) {
                Bitmap.createScaledBitmap(orig, targetWidth, targetHeight, true)
            } else {
                orig
            }
            memoryCache.put(videoId, scaled)

            // Save to disk cache asynchronously
            try {
                FileOutputStream(diskCacheFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
            } catch (_: Exception) {}

            return@withContext scaled
        }

        null
    }
}
