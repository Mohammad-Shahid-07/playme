package com.playme.player.core.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.model.TrackType

@OptIn(UnstableApi::class)
object ExoPlayerTrackHelper {

    fun extractTracks(tracks: Tracks): Pair<List<MediaTrackInfo>, List<MediaTrackInfo>> {
        val audioList = mutableListOf<MediaTrackInfo>()
        val subList = mutableListOf<MediaTrackInfo>()

        for ((groupIndex, group) in tracks.groups.withIndex()) {
            val trackType = group.type
            val mediaTrackGroup = group.mediaTrackGroup
            for (i in 0 until mediaTrackGroup.length) {
                val format = mediaTrackGroup.getFormat(i)
                val isSelected = group.isTrackSelected(i)
                val label = format.label ?: format.language ?: "Track ${i + 1}"
                val id = trackId(trackType, groupIndex, i, format.id)

                if (trackType == C.TRACK_TYPE_AUDIO) {
                    audioList.add(
                        MediaTrackInfo(
                            id = id,
                            name = label,
                            language = format.language,
                            type = TrackType.AUDIO,
                            isSelected = isSelected
                        )
                    )
                } else if (trackType == C.TRACK_TYPE_TEXT) {
                    subList.add(
                        MediaTrackInfo(
                            id = id,
                            name = label,
                            language = format.language,
                            type = TrackType.SUBTITLE,
                            isSelected = isSelected
                        )
                    )
                }
            }
        }
        return Pair(audioList, subList)
    }

    fun selectAudioTrack(selector: DefaultTrackSelector?, player: ExoPlayer?, trackId: String) {
        val defaultSelector = selector ?: return
        if (trackId == "default") {
            defaultSelector.parameters = defaultSelector.buildUponParameters()
                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                .build()
            return
        }
        val currentTracks = player?.currentTracks ?: return

        for ((groupIndex, group) in currentTracks.groups.withIndex()) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                val mediaTrackGroup = group.mediaTrackGroup
                for (i in 0 until mediaTrackGroup.length) {
                    val format = mediaTrackGroup.getFormat(i)
                    val id = trackId(group.type, groupIndex, i, format.id)
                    if (id == trackId) {
                        defaultSelector.parameters = defaultSelector.buildUponParameters()
                            .setOverrideForType(TrackSelectionOverride(mediaTrackGroup, i))
                            .build()
                        return
                    }
                }
            }
        }
    }

    fun selectSubtitleTrack(selector: DefaultTrackSelector?, player: ExoPlayer?, trackId: String) {
        val defaultSelector = selector ?: return
        if (trackId == "none" || trackId == "-1") {
            defaultSelector.parameters = defaultSelector.buildUponParameters()
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            return
        }
        val currentTracks = player?.currentTracks ?: return
        for ((groupIndex, group) in currentTracks.groups.withIndex()) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val mediaTrackGroup = group.mediaTrackGroup
                for (i in 0 until mediaTrackGroup.length) {
                    val format = mediaTrackGroup.getFormat(i)
                    val id = trackId(group.type, groupIndex, i, format.id)
                    if (id == trackId) {
                        defaultSelector.parameters = defaultSelector.buildUponParameters()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .setOverrideForType(TrackSelectionOverride(mediaTrackGroup, i))
                            .build()
                        return
                    }
                }
            }
        }
    }

    private fun trackId(trackType: Int, groupIndex: Int, trackIndex: Int, formatId: String?): String =
        formatId ?: "$trackType-$groupIndex-$trackIndex"
}
