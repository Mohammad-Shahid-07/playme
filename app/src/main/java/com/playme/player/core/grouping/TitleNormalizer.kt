package com.playme.player.core.grouping

import java.util.Locale

/**
 * Turns a raw filename into a comparable title stem.
 *
 * This is the part VLC gets wrong: it compares the first few characters, so
 * `S01_ShowA_E01` and `S01_ShowB_E01` both start with `S01_` and get merged.
 * Here the season/episode markers are removed *before* comparison, so only the
 * actual show name remains.
 */
internal object TitleNormalizer {

    private val EXTENSION = Regex("\\.[A-Za-z0-9]{2,4}$")
    private val RELEASE_GROUP = Regex("\\s*-\\s*(?=[A-Za-z0-9@]*[A-Za-z@])[A-Za-z0-9@]{2,}$")
    /** Telegram/encoder handles such as `@AnimeChidori`. */
    private val AT_HANDLE = Regex("@[\\p{L}\\p{N}_.]+")
    private val BRACKETS = Regex("[\\[\\]{}()]")
    private val SEPARATORS = Regex("[._\\-–—:;,+/\\\\|]+")
    private val NON_WORD = Regex("[^\\p{L}\\p{N}\\s]+")
    private val MULTISPACE = Regex("\\s+")
    private val YEAR = Regex("\\b(19\\d{2}|20\\d{2})\\b")
    private val COMPOUND_TAG_REGEX = Regex(
        "\\b(?:web[-.\\s]dl|blu[-.\\s]ray|hd[-.\\s]rip|web[-.\\s]rip|dvd[-.\\s]rip|bd[-.\\s]rip|br[-.\\s]rip)\\b",
        RegexOption.IGNORE_CASE
    )

    /** Compound tags must be collapsed before separators are turned into spaces. */
    private val COMPOUND_TAGS = listOf(
        "web-dl" to "webdl",
        "web dl" to "webdl",
        "blu-ray" to "bluray",
        "blu ray" to "bluray",
        "hd-rip" to "hdrip",
        "web-rip" to "webrip",
        "dvd-rip" to "dvdrip",
        "bd-rip" to "bdrip",
        "br-rip" to "brrip"
    )

    /**
     * Release/codec noise that never belongs to a title.
     * Kept deliberately conservative: stripping real words is worse than
     * leaving one tag in place, because it can merge unrelated shows.
     */
    private val TECHNICAL_TOKENS = setOf(
        "480p", "540p", "576p", "720p", "1080p", "1440p", "2160p", "4k", "8k",
        "uhd", "fhd", "hdr", "hdr10", "hdr10plus", "dv", "dolby", "atmos", "truehd",
        "x264", "x265", "h264", "h265", "hevc", "avc", "xvid", "divx", "10bit", "8bit",
        "aac", "ac3", "eac3", "dts", "ddp", "opus", "mp3", "flac", "2ch", "6ch", "8ch",
        "av1", "vp9",
        "web", "webdl", "webrip", "bluray", "brrip", "bdrip", "dvdrip", "hdtv", "pdtv",
        "hdcam", "camrip", "hdrip", "remux", "proper", "repack", "extended", "uncut",
        "remastered", "dubbed", "subbed", "dual", "yts", "yify", "rarbg", "psa", "evo", "ntb",
        "tigole", "qxr", "fgt", "subsplease", "erai", "horriblesubs",
        "amzn", "dsnp", "atvp", "hmax", "pcok", "itunes"
    )

    fun normalize(raw: String): NormalizedText {
        val lower = toComparableText(raw)
        val year = YEAR.find(lower)?.value?.toIntOrNull()
        val yearToken = year?.toString()
        val tokens = filterTokens(lower, yearToken)
        return NormalizedText(text = lower, tokens = tokens, year = year)
    }

    /** Strips technical release tags and the detected year token from comparable text. */
    fun filterTokens(comparableText: String, yearToken: String?): List<String> =
        comparableText.split(' ')
            .filter { it.isNotEmpty() && it !in TECHNICAL_TOKENS && it != yearToken }

    /**
     * Lowercase, separator-free version of the filename. Episode markers are
     * still present here — the parser removes them next.
     */
    fun toComparableText(raw: String): String {
        var text = raw.trim()
        text = EXTENSION.replace(text, "")
        text = RELEASE_GROUP.replace(text, "")
        text = AT_HANDLE.replace(text, " ")
        var lower = text.lowercase(Locale.ROOT)
        COMPOUND_TAGS.forEach { (from, to) -> lower = lower.replace(from, to) }
        lower = BRACKETS.replace(lower, " ")
        lower = SEPARATORS.replace(lower, " ")
        lower = NON_WORD.replace(lower, " ")
        return MULTISPACE.replace(lower, " ").trim()
    }

    /**
     * Human-facing title with markers and technical tags stripped.
     * Original casing is preserved unless the filename was uniformly
     * lowercase/uppercase, in which case it is title-cased.
     */
    fun displayName(raw: String, markerTokens: Set<String>): String {
        var text = raw.trim()
        text = EXTENSION.replace(text, "")
        text = RELEASE_GROUP.replace(text, "")
        text = AT_HANDLE.replace(text, " ")
        text = COMPOUND_TAG_REGEX.replace(text, " ")
        text = BRACKETS.replace(text, " ")
        text = SEPARATORS.replace(text, " ")
        text = NON_WORD.replace(text, " ")

        val kept = text.split(' ')
            .filter { it.isNotBlank() }
            .filter { token ->
                val lower = token.lowercase(Locale.ROOT)
                lower !in TECHNICAL_TOKENS && lower !in markerTokens
            }

        if (kept.isEmpty()) return raw.substringBeforeLast('.').ifBlank { raw }

        val allLower = kept.all { it == it.lowercase(Locale.ROOT) }
        val allUpper = kept.all { it == it.uppercase(Locale.ROOT) }
        val joined = kept.joinToString(" ").trim()

        return when {
            allLower -> joined.split(' ').joinToString(" ") { word ->
                word.replaceFirstChar { it.titlecase(Locale.ROOT) }
            }
            allUpper -> joined.split(' ').joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
            }
            else -> joined
        }
    }
}

internal data class NormalizedText(
    val text: String,
    val tokens: List<String>,
    val year: Int?
)
