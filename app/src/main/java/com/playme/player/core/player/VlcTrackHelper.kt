package com.playme.player.core.player

import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.model.TrackType
import org.videolan.libvlc.MediaPlayer

object VlcTrackHelper {

    fun extractTracks(mp: MediaPlayer?): Pair<List<MediaTrackInfo>, List<MediaTrackInfo>> {
        if (mp == null) return Pair(emptyList(), emptyList())
        val currentAudio = mp.audioTrack
        val currentSpu = mp.spuTrack

        val audioList = mp.audioTracks?.map { track ->
            MediaTrackInfo(
                id = track.id.toString(),
                name = track.name.ifBlank { "Track ${track.id}" },
                type = TrackType.AUDIO,
                isSelected = track.id == currentAudio
            )
        } ?: emptyList()

        val spuList = mp.spuTracks?.map { track ->
            MediaTrackInfo(
                id = track.id.toString(),
                name = if (track.id == -1) "Disable Subtitles" else track.name.ifBlank { "Subtitle ${track.id}" },
                type = TrackType.SUBTITLE,
                isSelected = track.id == currentSpu
            )
        } ?: emptyList()

        return Pair(audioList, spuList)
    }
}
