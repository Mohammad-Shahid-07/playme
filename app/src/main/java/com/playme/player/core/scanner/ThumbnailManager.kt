package com.playme.player.core.scanner

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.util.LruCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
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
     * Back off failed decodes instead of poisoning them permanently: fast
     * scrolling stays smooth, while files that were still copying or blocked
     * before a permission grant can recover after a short cooldown.
     */
    private data class FailureRecord(val attempts: Int, val failedAtMs: Long)

    private const val SHORT_RETRY_COOLDOWN_MS = 30_000L
    private const val LONG_RETRY_COOLDOWN_MS = 5 * 60_000L
    private const val MAX_FAST_ATTEMPTS = 3
    private const val MAX_FAILURE_ENTRIES = 1_000

    private val failures = ConcurrentHashMap<String, FailureRecord>()
    private val keyLocks = Array(64) { Mutex() }

    private fun lockFor(key: String): Mutex = keyLocks[key.hashCode() and 0x3F]

    private fun isInFailureCooldown(videoId: String): Boolean {
        val record = failures[videoId] ?: return false
        val cooldown = if (record.attempts >= MAX_FAST_ATTEMPTS) {
            LONG_RETRY_COOLDOWN_MS
        } else {
            SHORT_RETRY_COOLDOWN_MS
        }
        return SystemClock.elapsedRealtime() - record.failedAtMs < cooldown
    }

    private fun recordFailure(videoId: String) {
        if (failures.size > MAX_FAILURE_ENTRIES) failures.clear()
        val now = SystemClock.elapsedRealtime()
        val prev = failures[videoId]
        failures[videoId] = FailureRecord(
            attempts = (prev?.attempts ?: 0) + 1,
            failedAtMs = now
        )
    }

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
        if (isInFailureCooldown(videoId)) return null

        return lockFor(key).withLock {
            memoryCache.get(key)?.let { return@withLock it }
            if (isInFailureCooldown(videoId)) return@withLock null

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
                recordFailure(videoId)
            } else {
                failures.remove(videoId)
                memoryCache.put(key, bitmap)
            }
            bitmap
        }
    }
}
