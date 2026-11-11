package com.playme.player.core.grouping

import com.playme.player.core.model.SeriesEpisode
import com.playme.player.core.model.SeriesItem
import com.playme.player.core.model.VideoItem
import com.playme.player.core.util.formatBytes
import com.playme.player.core.util.formatEpisodeLabel
import kotlin.math.max

/**
 * Clusters videos into series without the prefix-matching trap.
 *
 * Rules, in order of importance:
 * 1. The comparison key is the title **after** season/episode markers are
 *    stripped, so `S01_ShowA_E01` and `S01_ShowB_E01` stay separate.
 * 2. A series needs at least two episodes. Single marked files are never
 *    promoted to a series on their own.
 * 3. Fuzzy matching only *folds a lone episode into an established series*.
 *    Two unrelated singles are never merged, so a wrong group requires an
 *    already-confident group to exist first.
 * 4. Different known years (remake vs original) never merge.
 */
object SeriesGrouper {

    private const val MIN_EPISODES = 2
    private const val LEVENSHTEIN_MIN = 0.88f
    private const val JACCARD_MIN = 0.70f
    private const val NEAR_IDENTICAL = 0.95f
    private const val MAX_COMPARE_LENGTH = 48

    fun group(videos: List<VideoItem>): List<SeriesItem> {
        if (videos.isEmpty()) return emptyList()

        val buckets = LinkedHashMap<String, MutableList<Entry>>()
        for (video in videos) {
            val info = EpisodeParser.parse(video.title)
            if (!info.hasMarker || info.core.isBlank()) continue
            buckets.getOrPut(signature(info)) { mutableListOf() } += Entry(video, info)
        }
        if (buckets.isEmpty()) return emptyList()

        val seriesBuckets = ArrayList<MutableList<Entry>>()
        val lonely = ArrayList<Entry>()
        buckets.values.forEach { bucket ->
            if (bucket.size >= MIN_EPISODES) seriesBuckets += bucket else lonely += bucket.first()
        }
        if (seriesBuckets.isEmpty()) return emptyList()

        val index = HashMap<String, MutableList<Int>>()
        seriesBuckets.forEachIndexed { position, bucket ->
            index.getOrPut(blockKey(bucket.first().info.core)) { mutableListOf() } += position
        }

        lonely.forEach { single ->
            val target = bestMatch(single.info, seriesBuckets, index) ?: return@forEach
            seriesBuckets[target] += single
        }

        return seriesBuckets
            .map { buildSeries(it) }
            .sortedByDescending { it.latestModifiedMs }
    }

    private fun bestMatch(
        info: EpisodeInfo,
        buckets: List<MutableList<Entry>>,
        index: Map<String, MutableList<Int>>
    ): Int? {
        val candidates = index[blockKey(info.core)] ?: return null
        var bestScore = 0f
        var bestIndex = -1
        candidates.forEach { position ->
            val score = similarity(info, buckets[position].first().info)
            if (score > bestScore) {
                bestScore = score
                bestIndex = position
            }
        }
        return if (bestScore > 0f) bestIndex else null
    }

    private fun similarity(a: EpisodeInfo, b: EpisodeInfo): Float {
        if (a.year != null && b.year != null && a.year != b.year) return 0f
        if (a.core == b.core) return 1f

        val jaccard = jaccard(a.core, b.core)
        val levenshtein = levenshteinRatio(a.core, b.core)
        return when {
            jaccard >= JACCARD_MIN && levenshtein >= LEVENSHTEIN_MIN -> max(jaccard, levenshtein)
            jaccard == 0f && levenshtein >= NEAR_IDENTICAL -> levenshtein
            else -> 0f
        }
    }

    private fun jaccard(a: String, b: String): Float {
        val left = a.split(' ').filter { it.isNotBlank() }.toSet()
        val right = b.split(' ').filter { it.isNotBlank() }.toSet()
        if (left.isEmpty() || right.isEmpty()) return 0f
        val intersection = left.count { it in right }
        val union = left.size + right.size - intersection
        return if (union == 0) 0f else intersection.toFloat() / union
    }

    private fun levenshteinRatio(a: String, b: String): Float {
        val left = a.take(MAX_COMPARE_LENGTH)
        val right = b.take(MAX_COMPARE_LENGTH)
        val longest = max(left.length, right.length)
        if (longest == 0) return 1f

        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)

        for (i in 1..left.length) {
            current[0] = i
            for (j in 1..right.length) {
                val cost = if (left[i - 1] == right[j - 1]) 0 else 1
                current[j] = minOf(
                    current[j - 1] + 1,
                    previous[j] + 1,
                    previous[j - 1] + cost
                )
            }
            val swap = previous
            previous = current
            current = swap
        }
        return 1f - previous[right.length].toFloat() / longest
    }

    private fun buildSeries(entries: List<Entry>): SeriesItem {
        val sorted = entries.sortedWith(
            compareBy<Entry> { it.info.season ?: Int.MAX_VALUE }
                .thenBy { it.info.episode ?: Int.MAX_VALUE }
                .thenBy { it.info.part ?: 0 }
                .thenBy { it.video.title.lowercase() }
        )

        val title = representativeTitle(sorted)

        val episodes = sorted.map { entry ->
            SeriesEpisode(
                video = entry.video,
                season = entry.info.season,
                episode = entry.info.episode,
                part = entry.info.part,
                label = formatEpisodeLabel(entry.info.season, entry.info.episode, entry.info.part),
                title = episodeTitle(entry.info, title)
            )
        }

        val totalBytes = episodes.sumOf { it.video.sizeBytes }
        val seasons = episodes.mapNotNull { it.season }.distinct().sorted()
        val first = sorted.first()

        return SeriesItem(
            id = "series_" + signature(first.info).hashCode().toString(),
            title = title,
            year = first.info.year,
            seasons = seasons,
            episodes = episodes,
            totalSizeBytes = totalBytes,
            totalSizeFormatted = formatBytes(totalBytes),
            latestModifiedMs = episodes.maxOf { it.video.dateModifiedMs }
        )
    }

    /**
     * Episode rows should not repeat the series name or the release tags.
     * `[01-12] Jobless Reincarnation [720p]` becomes `Episode 12`, while
     * `Show.S01E02.The.Offer` keeps `The Offer`.
     */
    private fun episodeTitle(info: EpisodeInfo, seriesTitle: String): String {
        val display = info.displayTitle.trim()
        val fallback = info.episode?.let { "Episode $it" }
            ?: info.part?.let { "Part $it" }
            ?: display

        if (display.equals(seriesTitle, ignoreCase = true)) return fallback
        if (display.startsWith(seriesTitle, ignoreCase = true)) {
            val rest = display.substring(seriesTitle.length)
                .trim(' ', '-', '–', '—', ':', '.', ',')
            if (rest.isNotEmpty()) return rest
            return fallback
        }
        return display.ifBlank { fallback }
    }

    /**
     * The series name is the longest token prefix every episode agrees on, so
     * `Show Name The Offer` / `Show Name The Pilot` resolve to `Show Name`
     * instead of whichever filename happens to be longest.
     */
    private fun representativeTitle(entries: List<Entry>): String {
        val titles = entries.map { it.info.displayTitle.trim() }.filter { it.isNotBlank() }
        if (titles.isEmpty()) return entries.first().info.core
        if (titles.size == 1) return titles.first()

        val tokenLists = titles.map { it.split(' ').filter(String::isNotBlank) }
        val shortest = tokenLists.minOf { it.size }
        var common = 0
        while (common < shortest) {
            val token = tokenLists.first()[common].lowercase()
            if (tokenLists.all { it[common].lowercase() == token }) common++ else break
        }

        if (common >= 1) return tokenLists.first().take(common).joinToString(" ")

        return titles.groupBy { it.lowercase() }
            .maxByOrNull { it.value.size }
            ?.value
            ?.first()
            ?: titles.first()
    }

    private fun signature(info: EpisodeInfo): String = "${info.core}|${info.year ?: ""}"

    private fun blockKey(core: String): String =
        core.filter { it.isLetterOrDigit() }.take(4)

    private data class Entry(val video: VideoItem, val info: EpisodeInfo)
}
