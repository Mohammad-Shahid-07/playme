package com.playme.player.core.model

import androidx.compose.runtime.Immutable

/**
 * One episode inside an auto-detected series.
 *
 * [season], [episode] and [part] are nullable because real-world filenames are
 * inconsistent: some files carry a full SxxExx marker, some only an episode
 * number, some only "Part 2".
 */
@Immutable
data class SeriesEpisode(
    val video: VideoItem,
    val season: Int? = null,
    val episode: Int? = null,
    val part: Int? = null,
    val label: String = "",
    /** Cleaned display name with the series title and release tags removed. */
    val title: String = ""
)

/**
 * A group of videos that share the same title stem.
 *
 * Grouping is season-agnostic on purpose: `Show.S01E01` and `Show.S02E04`
 * belong to the same series, seasons are just metadata on the episodes.
 */
@Immutable
data class SeriesItem(
    val id: String,
    val title: String,
    val year: Int? = null,
    val seasons: List<Int> = emptyList(),
    val episodes: List<SeriesEpisode> = emptyList(),
    val totalSizeBytes: Long = 0L,
    val totalSizeFormatted: String = "",
    val latestModifiedMs: Long = 0L
) {
    val episodeCount: Int get() = episodes.size

    val totalDurationMs: Long get() = episodes.sumOf { it.video.durationMs }

    /** "S01" for a single season, "S01–S03" for a span, "Episodes" when unknown. */
    val seasonLabel: String
        get() = when {
            seasons.isEmpty() -> "Episodes"
            seasons.size == 1 -> "S%02d".format(seasons.first())
            else -> "S%02d–S%02d".format(seasons.min(), seasons.max())
        }

    val metaLine: String
        get() = buildString {
            append(seasonLabel)
            append("  ·  ")
            append(if (episodeCount == 1) "1 episode" else "$episodeCount episodes")
            if (totalSizeFormatted.isNotEmpty()) {
                append("  ·  ")
                append(totalSizeFormatted)
            }
        }
}
