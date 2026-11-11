package com.playme.player.core.util

import java.util.Locale

/** Human readable byte size, e.g. `1.4 GB` / `820 MB` / `612 KB`. */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return "%.0f KB".format(Locale.US, kb)
    val mb = kb / 1024.0
    return if (mb >= 1000.0) {
        "%.1f GB".format(Locale.US, mb / 1024.0)
    } else {
        "%.0f MB".format(Locale.US, mb)
    }
}

/** `1:02:03` above an hour, `02:03` below it. */
fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSec / 3600L
    val minutes = (totalSec % 3600L) / 60L
    val seconds = totalSec % 60L
    return if (hours > 0) {
        "%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    } else {
        "%02d:%02d".format(Locale.US, minutes, seconds)
    }
}

/**
 * Episode chip text: `S01E04` when both parts are known, `E04` when only the
 * episode is, `Part 2` for split files, empty when nothing was detected.
 */
fun formatEpisodeLabel(season: Int?, episode: Int?, part: Int?): String = when {
    season != null && episode != null -> "S%02dE%02d".format(Locale.US, season, episode)
    episode != null -> "E%02d".format(Locale.US, episode)
    part != null -> "Part %d".format(Locale.US, part)
    else -> ""
}
