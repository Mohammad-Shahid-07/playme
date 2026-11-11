package com.playme.player.feature.player.ui

import androidx.compose.runtime.Composable
import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import kotlin.math.abs

@Composable
internal fun PlayerSettingsSubViewContent(
    currentView: String,
    audioDelayMs: Long,
    subtitleDelayMs: Long,
    decoderMode: DecoderMode,
    aspectRatioMode: AspectRatioMode,
    sleepTimerMinutes: Int,
    playbackSpeed: Float,
    selectedAudio: String,
    selectedSubtitle: String,
    subtitleSize: String,
    availableAudioTracks: List<MediaTrackInfo>,
    availableSubtitleTracks: List<MediaTrackInfo>,
    onBack: () -> Unit,
    onAdjustAudioDelay: (Long) -> Unit,
    onAdjustSubtitleDelay: (Long) -> Unit,
    onSelectDecoderMode: (DecoderMode) -> Unit,
    onSelectAspectRatio: (AspectRatioMode) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onSelectSleepTimer: (Int) -> Unit,
    onSelectAudioTrack: (String) -> Unit,
    onSelectSubtitleTrack: (String) -> Unit,
    onSelectSubtitleSize: (String) -> Unit
) {
    when (currentView) {
        "audio-track" -> AudioTrackSubView(
            tracks = availableAudioTracks,
            selectedAudio = selectedAudio,
            onSelect = { onSelectAudioTrack(it); onBack() },
            onBack = onBack
        )

        "audio-sync" -> SettingsSyncSubView(
            title = "Audio Sync",
            delayMs = audioDelayMs,
            isAudio = true,
            onAdjustDelay = onAdjustAudioDelay,
            onReset = { if (audioDelayMs != 0L) onAdjustAudioDelay(-audioDelayMs) },
            onBack = onBack
        )

        "subtitles" -> SubtitleTrackSubView(
            tracks = availableSubtitleTracks,
            selectedSubtitle = selectedSubtitle,
            subtitleSize = subtitleSize,
            onSelectTrack = { onSelectSubtitleTrack(it); onBack() },
            onSelectSize = onSelectSubtitleSize,
            onBack = onBack
        )

        "subtitle-sync" -> SettingsSyncSubView(
            title = "Subtitle Sync",
            delayMs = subtitleDelayMs,
            isAudio = false,
            onAdjustDelay = onAdjustSubtitleDelay,
            onReset = { if (subtitleDelayMs != 0L) onAdjustSubtitleDelay(-subtitleDelayMs) },
            onBack = onBack
        )

        "speed" -> {
            val speedItems = listOf(
                ChoiceItem("0.5", "0.5x"),
                ChoiceItem("0.75", "0.75x"),
                ChoiceItem("1.0", "1.0x (Normal)"),
                ChoiceItem("1.25", "1.25x"),
                ChoiceItem("1.5", "1.5x"),
                ChoiceItem("1.75", "1.75x"),
                ChoiceItem("2.0", "2.0x")
            )
            val matchedSpeedId = speedItems.find {
                val target = it.id.toFloatOrNull() ?: 1.0f
                abs(playbackSpeed - target) < 0.01f
            }?.id ?: playbackSpeed.toString()

            SettingsChoiceSubView(
                title = "Playback Speed",
                items = speedItems,
                selectedId = matchedSpeedId,
                onSelect = { onSelectSpeed(it.toFloatOrNull() ?: 1.0f); onBack() },
                onBack = onBack
            )
        }

        "decoder" -> SettingsChoiceSubView(
            title = "Hardware Decoder",
            items = listOf(
                ChoiceItem("HW", "HW (Media3 ExoPlayer)", "Native hardware decoding"),
                ChoiceItem("SW", "SW (Universal LibVLC)", "FFmpeg software fallback for all formats")
            ),
            selectedId = decoderMode.name,
            onSelect = {
                onSelectDecoderMode(if (it == "SW") DecoderMode.SW else DecoderMode.HW)
                onBack()
            },
            onBack = onBack
        )

        "sleep-timer" -> SettingsChoiceSubView(
            title = "Sleep Timer",
            items = listOf(
                ChoiceItem("0", "Off"),
                ChoiceItem("15", "15 Minutes"),
                ChoiceItem("30", "30 Minutes"),
                ChoiceItem("45", "45 Minutes"),
                ChoiceItem("60", "60 Minutes")
            ),
            selectedId = sleepTimerMinutes.toString(),
            onSelect = { onSelectSleepTimer(it.toIntOrNull() ?: 0); onBack() },
            onBack = onBack
        )

        "aspect" -> SettingsChoiceSubView(
            title = "Aspect Ratio",
            items = AspectRatioMode.entries.map {
                ChoiceItem(
                    id = it.name,
                    title = it.label,
                    desc = when (it) {
                        AspectRatioMode.BEST_FIT -> "Preserves original aspect ratio with black bars"
                        AspectRatioMode.FIT_SCREEN -> "Zooms and crops video to fill the screen"
                        AspectRatioMode.FILL -> "Stretches video to fill all screen edges"
                        AspectRatioMode.ORIGINAL -> "Displays exact native pixel resolution"
                        AspectRatioMode.SIXTEEN_NINE -> "Locks to 16:9 widescreen ratio"
                        AspectRatioMode.FOUR_THREE -> "Locks to 4:3 standard ratio"
                    }
                )
            },
            selectedId = aspectRatioMode.name,
            onSelect = { name ->
                AspectRatioMode.entries.find { it.name == name }?.let { onSelectAspectRatio(it) }
                onBack()
            },
            onBack = onBack
        )
    }
}

@Composable
private fun AudioTrackSubView(
    tracks: List<MediaTrackInfo>,
    selectedAudio: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    val choices = if (tracks.isNotEmpty()) {
        tracks.map {
            ChoiceItem(
                id = it.id,
                title = it.name,
                desc = it.language?.let { lang -> "Language: $lang" } ?: "Audio Stream"
            )
        }
    } else {
        listOf(ChoiceItem("default", "Default Audio", "Stereo / Surround Auto-detect"))
    }
    val selectedId = tracks.find { it.id == selectedAudio || it.name == selectedAudio }?.id
        ?: if (tracks.isEmpty()) "default" else selectedAudio

    SettingsChoiceSubView(
        title = "Audio Track",
        items = choices,
        selectedId = selectedId,
        onSelect = onSelect,
        onBack = onBack
    )
}

@Composable
private fun SubtitleTrackSubView(
    tracks: List<MediaTrackInfo>,
    selectedSubtitle: String,
    subtitleSize: String,
    onSelectTrack: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onBack: () -> Unit
) {
    val choices = buildList {
        add(ChoiceItem("none", "Off", "Captions disabled"))
        tracks.forEach {
            add(
                ChoiceItem(
                    id = it.id,
                    title = it.name,
                    desc = it.language?.let { lang -> "Language: $lang" } ?: "Embedded Subtitle"
                )
            )
        }
    }
    val selectedId = when {
        selectedSubtitle.equals("none", ignoreCase = true) ||
            selectedSubtitle.equals("off", ignoreCase = true) -> "none"
        else -> tracks.find { it.id == selectedSubtitle || it.name == selectedSubtitle }?.id
            ?: selectedSubtitle
    }

    SettingsChoiceSubView(
        title = "Subtitles",
        items = choices,
        selectedId = selectedId,
        onSelect = onSelectTrack,
        onBack = onBack,
        extraSectionTitle = "SUBTITLE FONT SIZE",
        extraItems = listOf(
            ChoiceItem("Normal", "Normal"),
            ChoiceItem("Large", "Large"),
            ChoiceItem("Extra Large", "Extra Large")
        ),
        extraSelectedId = subtitleSize,
        onExtraSelect = onSelectSize
    )
}

internal fun resolveDisplayAudioLabel(
    selectedAudio: String,
    tracks: List<MediaTrackInfo>
): String {
    if (selectedAudio.equals("default", ignoreCase = true)) return "Default Audio"
    return tracks.find { it.id == selectedAudio }?.name ?: selectedAudio
}

internal fun resolveDisplaySubtitleLabel(
    selectedSubtitle: String,
    tracks: List<MediaTrackInfo>
): String {
    if (selectedSubtitle.equals("none", ignoreCase = true)) return "Off"
    return tracks.find { it.id == selectedSubtitle }?.name ?: selectedSubtitle
}
