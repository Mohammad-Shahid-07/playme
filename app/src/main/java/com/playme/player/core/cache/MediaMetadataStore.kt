package com.playme.player.core.cache

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Memoises the expensive `MediaMetadataRetriever` probe per file.
 *
 * The deep storage walk used to open every discovered video to read duration
 * and resolution — that is what made the first launch take so long. The key
 * includes size and mtime, so an edited file is re-probed automatically.
 */
class MediaMetadataStore(context: Context) {

    private val file = File(context.filesDir, FILE_NAME)
    private val entries = ConcurrentHashMap<String, Entry>()
    private val dirty = AtomicBoolean(false)

    @Volatile
    private var loaded = false

    data class Entry(val durationMs: Long, val resolution: String)

    fun load() {
        if (loaded) return
        loaded = true
        if (!file.exists()) return
        try {
            val payload = JSONObject(file.readText())
            payload.keys().forEach { key ->
                val value = payload.optJSONArray(key) ?: return@forEach
                entries[key] = Entry(
                    durationMs = value.optLong(0),
                    resolution = value.optString(1, "HD")
                )
            }
        } catch (_: Exception) {
            entries.clear()
        }
    }

    fun get(key: String): Entry? = entries[key]

    fun put(key: String, entry: Entry) {
        val previous = entries.put(key, entry)
        if (previous != entry) dirty.set(true)
    }

    /** Writes only when something changed; called once at the end of a scan. */
    fun flush() {
        if (!dirty.compareAndSet(true, false)) return
        try {
            if (entries.size > MAX_ENTRIES) {
                entries.clear()
                return
            }
            val payload = JSONObject()
            entries.forEach { (key, entry) ->
                payload.put(key, org.json.JSONArray().put(entry.durationMs).put(entry.resolution))
            }
            val temp = File(file.parentFile, "$FILE_NAME.tmp")
            temp.writeText(payload.toString())
            if (!temp.renameTo(file)) {
                file.writeText(payload.toString())
                temp.delete()
            }
        } catch (_: Exception) {
            // Memoisation is an optimisation, never a correctness requirement.
        }
    }

    companion object {
        private const val FILE_NAME = "media_metadata.json"
        private const val MAX_ENTRIES = 40_000

        fun keyFor(path: String, size: Long, modified: Long): String = "$path|$size|$modified"

        fun keyFor(file: File): String = keyFor(file.absolutePath, file.length(), file.lastModified())
    }
}
