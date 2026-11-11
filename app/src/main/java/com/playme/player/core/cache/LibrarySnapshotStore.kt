package com.playme.player.core.cache

import android.content.Context
import android.net.Uri
import com.playme.player.core.model.BadgeVariant
import com.playme.player.core.model.VideoBadge
import com.playme.player.core.model.VideoItem
import com.playme.player.core.util.formatBytes
import com.playme.player.core.util.formatDuration
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persists the last completed scan so the library paints instantly on launch
 * instead of waiting for MediaStore + the recursive storage walk.
 *
 * Plain JSON on purpose: the payload is a flat list, and this avoids a Room
 * migration for data that is disposable and rebuilt on every full scan.
 */
class LibrarySnapshotStore(context: Context) {

    private val file = File(context.filesDir, FILE_NAME)
    private val lock = Any()

    fun read(): List<VideoItem> {
        if (!file.exists()) return emptyList()
        return try {
            val payload = JSONObject(file.readText())
            if (payload.optInt(KEY_VERSION, 0) != VERSION) return emptyList()
            val array = payload.optJSONArray(KEY_VIDEOS) ?: return emptyList()
            val now = System.currentTimeMillis()
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    array.optJSONObject(index)?.toVideoItem(now)?.let { add(it) }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun write(videos: List<VideoItem>) {
        if (videos.isEmpty()) return
        synchronized(lock) {
            try {
                val array = JSONArray()
                videos.forEach { array.put(it.toJson()) }
                val payload = JSONObject()
                    .put(KEY_VERSION, VERSION)
                    .put(KEY_SAVED_AT, System.currentTimeMillis())
                    .put(KEY_VIDEOS, array)

                val temp = File(file.parentFile, "$FILE_NAME.tmp")
                temp.writeText(payload.toString())
                if (!temp.renameTo(file)) {
                    file.writeText(payload.toString())
                    temp.delete()
                }
            } catch (_: Exception) {
                // A corrupt cache is never fatal: the next scan rebuilds it.
            }
        }
    }

    private fun VideoItem.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("uri", uri.toString())
        put("path", path)
        put("durationMs", durationMs)
        put("sizeBytes", sizeBytes)
        put("format", format)
        put("resolution", resolution)
        put("dateModifiedMs", dateModifiedMs)
        put("thumbnailUrl", thumbnailUrl.orEmpty())
        put("parentFolder", parentFolder)
        put(
            "badges",
            JSONArray().apply {
                badges.forEach { badge ->
                    put(JSONObject().put("label", badge.label).put("variant", badge.variant.name))
                }
            }
        )
    }

    private fun JSONObject.toVideoItem(now: Long): VideoItem? {
        val id = optString("id").takeIf { it.isNotBlank() } ?: return null
        val durationMs = optLong("durationMs")
        val sizeBytes = optLong("sizeBytes")
        val modified = optLong("dateModifiedMs")
        val badges = optJSONArray("badges")?.let { array ->
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val badge = array.optJSONObject(index) ?: continue
                    val label = badge.optString("label").takeIf { it.isNotBlank() } ?: continue
                    val variant = runCatching {
                        BadgeVariant.valueOf(badge.optString("variant"))
                    }.getOrDefault(BadgeVariant.GRAY)
                    add(VideoBadge(label, variant))
                }
            }
        }.orEmpty()

        return VideoItem(
            id = id,
            title = optString("title"),
            uri = Uri.parse(optString("uri")),
            path = optString("path"),
            durationMs = durationMs,
            durationFormatted = formatDuration(durationMs),
            sizeBytes = sizeBytes,
            fileSizeFormatted = formatBytes(sizeBytes),
            format = optString("format", "MP4"),
            resolution = optString("resolution", "HD"),
            thumbnailUrl = optString("thumbnailUrl").takeIf { it.isNotBlank() },
            dateModifiedMs = modified,
            isNew = modified > 0L && now - modified < NEW_WINDOW_MS,
            badges = badges,
            parentFolder = optString("parentFolder")
        )
    }

    private companion object {
        const val FILE_NAME = "library_snapshot.json"
        const val VERSION = 1
        const val KEY_VERSION = "version"
        const val KEY_SAVED_AT = "savedAt"
        const val KEY_VIDEOS = "videos"
        const val NEW_WINDOW_MS = 86_400_000L * 3
    }
}
