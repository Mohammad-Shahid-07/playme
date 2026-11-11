package com.playme.player.feature.player.vm

import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import com.playme.player.feature.player.model.PlayerState

object PlayerSettingsDelegate {

    fun cycleAspectRatio(current: AspectRatioMode): AspectRatioMode {
        val all = AspectRatioMode.entries
        return all[(all.indexOf(current) + 1) % all.size]
    }

    fun toggleDecoder(current: DecoderMode): DecoderMode {
        return if (current == DecoderMode.HW) DecoderMode.SW else DecoderMode.HW
    }

    fun findAudioTrack(tracks: List<MediaTrackInfo>, query: String): MediaTrackInfo? {
        return tracks.firstOrNull { it.name == query || it.id == query }
    }

    fun findSubtitleTrack(tracks: List<MediaTrackInfo>, query: String): MediaTrackInfo? {
        return tracks.firstOrNull { it.name == query || it.id == query }
    }

    fun createAbLoopState(state: PlayerState): PlayerState {
        val active = !state.isAbLoopActive
        return state.copy(
            isAbLoopActive = active,
            abLoopStartMs = if (active) state.currentPositionMs else null,
            abLoopEndMs = if (active) (state.currentPositionMs + 5000L).coerceAtMost(state.durationMs) else null
        )
    }

    fun applyAudioDelay(state: PlayerState, deltaMs: Long, engine: PlayerEngineContract?): Pair<PlayerState, String> {
        val next = PlayerDelaysDelegate.stepAudio(state.audioDelayMs, deltaMs)
        engine?.setAudioDelay(next)
        return state.copy(audioDelayMs = next) to "Audio delay: ${next}ms"
    }

    fun applySubtitleDelay(state: PlayerState, deltaMs: Long, engine: PlayerEngineContract?): Pair<PlayerState, String> {
        val next = PlayerDelaysDelegate.stepSubtitle(state.subtitleDelayMs, deltaMs)
        engine?.setSubtitleDelay(next)
        return state.copy(subtitleDelayMs = next) to "Subtitle delay: ${next}ms"
    }

    fun resetDelays(state: PlayerState, engine: PlayerEngineContract?): Pair<PlayerState, String> {
        engine?.setAudioDelay(0L)
        engine?.setSubtitleDelay(0L)
        return state.copy(audioDelayMs = 0L, subtitleDelayMs = 0L) to "Delays Reset"
    }

    fun selectAudio(state: PlayerState, trackNameOrId: String, engine: PlayerEngineContract?): Pair<PlayerState, String> {
        val track = findAudioTrack(state.availableAudioTracks, trackNameOrId)
        val idToSelect = track?.id ?: trackNameOrId
        val name = track?.name ?: if (idToSelect == "default") "Default Audio" else trackNameOrId
        engine?.selectAudioTrack(idToSelect)
        return state.copy(selectedAudioTrack = idToSelect) to "Audio: $name"
    }

    fun selectSubtitle(state: PlayerState, trackNameOrId: String, engine: PlayerEngineContract?): Pair<PlayerState, String> {
        val track = findSubtitleTrack(state.availableSubtitleTracks, trackNameOrId)
        val idToSelect = track?.id ?: if (trackNameOrId.equals("Off", ignoreCase = true) || trackNameOrId == "-1") "none" else trackNameOrId
        val name = track?.name ?: if (idToSelect == "none") "Off" else trackNameOrId
        engine?.selectSubtitleTrack(idToSelect)
        return state.copy(selectedSubtitleTrack = idToSelect) to "Subtitle: $name"
    }

    fun toggleDialogueClarity(state: PlayerState): Pair<PlayerState, String> {
        val next = !state.isDialogueClarityActive
        return state.copy(isDialogueClarityActive = next) to if (next) "Dialogue Clarity ON" else "Dialogue Clarity OFF"
    }

    fun toggleAmbientGlow(state: PlayerState): Pair<PlayerState, String> {
        val next = !state.ambientGlowEnabled
        return state.copy(ambientGlowEnabled = next) to if (next) "Ambient Glow ON" else "Ambient Glow OFF"
    }

    fun toggleBackgroundPlay(state: PlayerState): Pair<PlayerState, String> {
        val next = !state.backgroundPlayEnabled
        return state.copy(backgroundPlayEnabled = next) to if (next) "Background Play ON" else "Background Play OFF"
    }
}
