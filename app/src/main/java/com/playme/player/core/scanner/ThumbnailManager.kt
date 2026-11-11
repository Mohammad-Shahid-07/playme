package com.playme.player.core.scanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Thumbnail cache and scheduler. Decoding itself lives in [ThumbnailDecoder];
 * this type owns the memory cache, the on-disk cache location, concurrency and
 * failure bookkeeping.
 */
object ThumbnailManager {

    // Bump when the thumbnail algorithm changes so stale cache entries are
    // ignored rather than served.
    private const val CACHE_VERSION = "v5"

    private fun cacheKey(id: String) = "${CACHE_VERSION}_$id"

    /** Shared location for generated thumbnails; the player writes here too. */
    fun snapshotFile(context: Context, videoId: String): File =
        File(context.cacheDir, "thumb_${CACHE_VERSION}_$videoId.jpg")

    // 20% of memory for instant in-memory bitmap cache
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 5

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount / 1024
    }

    /**
     * Decoding is CPU-bound and must never compete with the UI thread. Two
     * threads at `THREAD_PRIORITY_BACKGROUND` mean the scheduler only runs them
     * when the main thread is idle.
     */
    private val decodeDispatcher: CoroutineDispatcher =
        Executors.newFixedThreadPool(2) { runnable ->
            Thread({
                android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_BACKGROUND
                )
                runnable.run()
            }, "playme-thumb").apply { isDaemon = true }
        }.asCoroutineDispatcher()

    /**
     * Files no decoder can handle fail on every attempt. Remembering that stops
     * the list from retrying a multi-second decode each time a card returns to
     * the viewport.
     */
    private val failedIds: MutableSet<String> =
        Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    fun getFromMemory(id: String): Bitmap? = memoryCache.get(cacheKey(id))

    suspend fun loadThumbnail(
        context: Context,
        videoId: String,
        uri: Uri,
        filePath: String,
        durationMs: Long = 0L,
        targetWidth: Int = 320,
        targetHeight: Int = 200
    ): Bitmap? {
        val key = cacheKey(videoId)
        memoryCache.get(key)?.let { return it }
        if (failedIds.contains(videoId)) return null

        val bitmap = withContext(decodeDispatcher) {
            ThumbnailDecoder.decode(
                context = context,
                videoId = videoId,
                uri = uri,
                filePath = filePath,
                durationMs = durationMs,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                diskCacheFile = snapshotFile(context, videoId)
            )
        }

        if (bitmap == null) {
            failedIds.add(videoId)
        } else {
            memoryCache.put(key, bitmap)
        }
        return bitmap
    }
}
