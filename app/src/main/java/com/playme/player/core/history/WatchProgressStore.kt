package com.playme.player.core.history

import android.content.Context
import com.playme.player.core.model.ContinueWatchingItem
import com.playme.player.core.model.VideoItem
import com.playme.player.core.util.formatDuration
import org.json.JSONObject
import java.io.File

/**
 * Real watch progress, replacing the fabricated percentages the library used
 * to show. Written when playback stops, read when the library builds its
 * "Continue Watching" row.
 */
class WatchProgressStore(context: Context) {

    private val file = File(context.filesDir, FILE_NAME)

    data class Progress(
        val videoId: String,
        val positionMs: Long,
        val durationMs: Long,
        val updatedAt: Long
    ) {
        val fraction: Float
            get() = if (durationMs <= 0L) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    }

    fun record(videoId: String, positionMs: Long, durationMs: Long) {
        if (videoId.isBlank() || positionMs <= 0L || durationMs <= 0L) return
        val all = readAll().toMutableMap()
        if (positionMs >= durationMs - FINISHED_MARGIN_MS) {
            all.remove(videoId)
        } else {
            all[videoId] = Progress(videoId, positionMs, durationMs, System.currentTimeMillis())
        }
        writeAll(all)
    }

    fun clear() {
        runCatching { file.delete() }
    }

    /** Most recent first, capped so the carousel stays light. */
    fun recent(limit: Int = 6): List<Progress> = readAll()
        .values
        .filter { it.fraction in MIN_FRACTION..MAX_FRACTION }
        .sortedByDescending { it.updatedAt }
        .take(limit)

    fun toContinueWatching(videos: List<VideoItem>, limit: Int = 6): List<ContinueWatchingItem> {
        val byId = videos.associateBy { it.id }
        return recent(limit).mapNotNull { progress ->
            val video = byId[progress.videoId] ?: return@mapNotNull null
            val leftMs = (progress.durationMs - progress.positionMs).coerceAtLeast(0L)
            ContinueWatchingItem(
                id = video.id,
                title = video.title,
                durationFormatted = formatDuration(progress.durationMs),
                timeLeftFormatted = "${formatDuration(leftMs)} left",
                progressPercent = progress.fraction * 100f,
                thumbnailUrl = video.thumbnailUrl,
                videoUri = video.uri,
                path = video.path
            )
        }
    }

    private fun readAll(): Map<String, Progress> {
        if (!file.exists()) return emptyMap()
        return try {
            val payload = JSONObject(file.readText())
            val result = HashMap<String, Progress>(payload.length())
            payload.keys().forEach { key ->
                val entry = payload.optJSONObject(key) ?: return@forEach
                result[key] = Progress(
                    videoId = key,
                    positionMs = entry.optLong("positionMs"),
                    durationMs = entry.optLong("durationMs"),
                    updatedAt = entry.optLong("updatedAt")
                )
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun writeAll(entries: Map<String, Progress>) {
        try {
            val payload = JSONObject()
            entries.forEach { (key, progress) ->
                payload.put(
                    key,
                    JSONObject()
                        .put("positionMs", progress.positionMs)
                        .put("durationMs", progress.durationMs)
                        .put("updatedAt", progress.updatedAt)
                )
            }
            val temp = File(file.parentFile, "$FILE_NAME.tmp")
            temp.writeText(payload.toString())
            if (!temp.renameTo(file)) {
                file.writeText(payload.toString())
                temp.delete()
            }
        } catch (_: Exception) {
            // Losing progress is preferable to crashing the library.
        }
    }

    private companion object {
        const val FILE_NAME = "watch_progress.json"
        const val FINISHED_MARGIN_MS = 5_000L
        const val MIN_FRACTION = 0.02f
        const val MAX_FRACTION = 0.95f
    }
}
