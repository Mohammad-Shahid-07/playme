package com.playme.player.core.grouping

import java.util.Locale

/**
 * Season/episode metadata pulled out of a filename, plus the title stem that
 * remains once every marker is removed.
 */
internal data class EpisodeInfo(
    val season: Int? = null,
    val episode: Int? = null,
    val part: Int? = null,
    val core: String = "",
    val year: Int? = null,
    val displayTitle: String = "",
    val markerTokens: Set<String> = emptySet()
) {
    val hasMarker: Boolean get() = season != null || episode != null || part != null
}

/**
 * Recognises the episode notations that actually show up on phones:
 * `S01E02`, `S01.E02`, `1x02`, `Season 1 Episode 2`, `EP02`, `Episode 2`,
 * `Part 1`, `CD1`, `Lecture 05`, and a plain trailing number.
 */
internal object EpisodeParser {

    /**
     * Leading `[Season-Episode]` prefix, e.g. `[01-13] Show [720p].mkv`.
     * Only accepted at the very start of the filename: mid-title dashes are
     * almost always part of the name, not an episode marker.
     */
    private val BRACKET_SEASON_EPISODE = Regex("^\\s*[\\[\\(](\\d{1,2})\\s*[-–]\\s*(\\d{1,3})[\\]\\)]")

    private val SEASON_EPISODE = listOf(
        Regex("\\bs(\\d{1,2})\\s*e(\\d{1,4})\\b"),
        Regex("\\bs(\\d{1,2})\\s*ep(\\d{1,4})\\b"),
        Regex("\\b(\\d{1,2})x(\\d{2,4})\\b"),
        Regex("\\bseason\\s*(\\d{1,2})\\s*(?:episode|ep)\\s*(\\d{1,4})\\b")
    )

    private val EPISODE_ONLY = listOf(
        Regex("\\bepisode\\s*(\\d{1,4})\\b"),
        Regex("\\bep\\s*(\\d{1,4})\\b"),
        Regex("\\be(\\d{1,4})\\b")
    )

    private val SEASON_WORD = Regex("\\bseason\\s*(\\d{1,2})\\b")
    private val SEASON_SHORT = Regex("\\bs(\\d{1,2})\\b")

    private val PART_MARKERS = listOf(
        Regex("\\bpart\\s*(\\d{1,3})\\b"),
        Regex("\\bpt\\s*(\\d{1,3})\\b"),
        Regex("\\bcd\\s*(\\d{1,2})\\b")
    )

    private val LECTURE = Regex("\\b(?:lecture|lec|lesson)\\s*(\\d{1,3})\\b")
    private val TRAILING_NUMBER = Regex("\\b(\\p{L}+)\\s+(\\d{1,3})\\s*$")

    /** "Class 10" is a grade, not an episode. */
    private val GRADE_WORDS = setOf("class", "grade", "std", "standard", "level", "year", "chapter", "unit")

    fun parse(rawTitle: String): EpisodeInfo {
        val prefix = BRACKET_SEASON_EPISODE.find(rawTitle)
        val workingTitle = if (prefix != null) rawTitle.removeRange(prefix.range) else rawTitle
        val normalized = TitleNormalizer.normalize(workingTitle)
        var work = normalized.text
        val markers = linkedSetOf<String>()

        var season: Int? = prefix?.groupValues?.get(1)?.toIntOrNull()
        var episode: Int? = prefix?.groupValues?.get(2)?.toIntOrNull()
        var part: Int? = null

        if (episode == null) {
            for (regex in SEASON_EPISODE) {
                val match = regex.find(work) ?: continue
                season = match.groupValues[1].toIntOrNull()
                episode = match.groupValues[2].toIntOrNull()
                markers += tokensOf(match.value)
                work = work.removeRange(match.range)
                break
            }
        }

        if (episode == null) {
            for (regex in EPISODE_ONLY) {
                val match = regex.find(work) ?: continue
                episode = match.groupValues[1].toIntOrNull()
                markers += tokensOf(match.value)
                work = work.removeRange(match.range)
                break
            }
        }

        if (season == null) {
            val match = SEASON_WORD.find(work) ?: SEASON_SHORT.find(work)
            if (match != null) {
                season = match.groupValues[1].toIntOrNull()
                markers += tokensOf(match.value)
                work = work.removeRange(match.range)
            }
        }

        for (regex in PART_MARKERS) {
            val match = regex.find(work) ?: continue
            part = match.groupValues[1].toIntOrNull()
            markers += tokensOf(match.value)
            work = work.removeRange(match.range)
            break
        }

        if (episode == null) {
            val lecture = parseLecture(work, markers)
            if (lecture != null) {
                episode = lecture.first
                work = lecture.second
            }
        }

        // A bare trailing number is the weakest signal, so it is held back
        // until the remaining stem is known: `VID 001` must not become a
        // series called "Vid", while `Show 01` legitimately should.
        var trailingEpisode: Int? = null
        var trailingToken: String? = null
        if (episode == null) {
            val match = TRAILING_NUMBER.find(work)
            if (match != null && match.groupValues[1].lowercase(Locale.ROOT) !in GRADE_WORDS) {
                trailingEpisode = match.groupValues[2].toIntOrNull()
                trailingToken = match.groupValues[2]
            }
        }

        var core = resolveSeriesCore(normalized, markers, prefix != null)
        val trailing = trailingEpisode
        if (episode == null && trailing != null) {
            val coreTokens = core.split(' ').filter { it.isNotBlank() }
            val stemTokens = if (coreTokens.lastOrNull() == trailingToken) {
                coreTokens.dropLast(1)
            } else {
                coreTokens
            }
            val looksLikeCameraPrefix = stemTokens.isEmpty() ||
                (stemTokens.size == 1 && stemTokens.first().length <= 3)
            if (!looksLikeCameraPrefix) {
                episode = trailing
                trailingToken?.let { markers += it }
                core = stemTokens.joinToString(" ")
            }
        }

        val displayMarkers = markers + normalized.year?.toString().orEmpty()

        return EpisodeInfo(
            season = season,
            episode = episode,
            part = part,
            core = core,
            year = normalized.year,
            displayTitle = TitleNormalizer.displayName(workingTitle, displayMarkers),
            markerTokens = markers
        )
    }

    /**
     * When an explicit episode/season marker follows the show title (e.g.
     * `VisionQuest.S01E02.Theseus`), tokens *after* the marker belong to the
     * episode title and release tags, not the series name. Prefix-style names
     * (`[01-13] Show`, `S01_ShowA_E01`) have nothing before the earliest marker
     * and fall back to all non-marker tokens.
     */
    private fun resolveSeriesCore(
        normalized: NormalizedText,
        markers: Set<String>,
        hadLeadingBracket: Boolean
    ): String {
        val fallback = normalized.tokens.filter { it !in markers }.joinToString(" ")
        if (hadLeadingBracket || markers.isEmpty()) return fallback

        val earliestStart = findEarliestMarkerStart(normalized.text) ?: return fallback
        if (earliestStart <= 0) return fallback

        val prefixTokens = TitleNormalizer
            .filterTokens(normalized.text.substring(0, earliestStart), normalized.year?.toString())
            .filter { it !in markers }
        return if (prefixTokens.isNotEmpty()) prefixTokens.joinToString(" ") else fallback
    }

    private fun findEarliestMarkerStart(text: String): Int? =
        (SEASON_EPISODE + EPISODE_ONLY + SEASON_WORD + SEASON_SHORT + PART_MARKERS + LECTURE)
            .mapNotNull { it.find(text)?.range?.first }
            .minOrNull()

    private fun parseLecture(work: String, markers: MutableSet<String>): Pair<Int, String>? {
        val match = LECTURE.find(work) ?: return null
        val number = match.groupValues[1].toIntOrNull() ?: return null
        markers += tokensOf(match.value)
        return number to work.removeRange(match.range)
    }

    private fun tokensOf(value: String): List<String> =
        value.split(' ').filter { it.isNotBlank() }
}
