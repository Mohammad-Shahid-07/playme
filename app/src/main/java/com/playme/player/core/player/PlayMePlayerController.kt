package com.playme.player.core.player

import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.playme.player.core.database.entity.VaultItemEntity
import com.playme.player.core.model.ABRepeatState
import com.playme.player.core.model.AspectRatioMode
import com.playme.player.core.model.AudioTrackItem
import com.playme.player.core.model.EqualizerBand
import com.playme.player.core.model.NerdStatsInfo
import com.playme.player.core.model.SleepTimerConfig
import com.playme.player.core.model.SubtitleTrackItem
import com.playme.player.core.model.VideoItem
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

enum class DecoderMode(val label: String) {
    HW("HW Decoder"),
    SW("SW Decoder")
}

data class PlayerUiState(
    val currentVideo: VideoItem? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isMuted: Boolean = false,
    val volumeBoostPercent: Int = 0, // 0 to 100% boost (100% to 200% total)
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isScreenLocked: Boolean = false,
    val isBackgroundPlayEnabled: Boolean = false,
    val availableSubtitles: List<SubtitleTrackItem> = emptyList(),
    val availableAudioTracks: List<AudioTrackItem> = emptyList(),
    val currentSubtitle: SubtitleTrackItem? = null,
    val currentAudioTrack: AudioTrackItem? = null,
    val decoderMode: DecoderMode = DecoderMode.HW,

    // Equalizer & Audio FX
    val equalizerEnabled: Boolean = true,
    val equalizerBands: List<EqualizerBand> = emptyList(),
    val currentEqPreset: String = "Flat",
    val bassBoostStrength: Int = 0, // 0 to 1000
    val virtualizerStrength: Int = 0, // 0 to 1000
    val isQuickAudioBoostActive: Boolean = false,

    // Audio / Subtitle Delay
    val audioDelayMs: Long = 0L,
    val subtitleDelayMs: Long = 0L,
    val isBluetoothDelayActive: Boolean = false,

    // A-B Repeat Looping
    val abRepeat: ABRepeatState = ABRepeatState(),

    // Smart Sleep Timer
    val sleepTimerConfig: SleepTimerConfig? = null,
    val sleepTimerRemainingSec: Long? = null,

    // Display & Filters
    val isMirrorMode: Boolean = false,
    val isNightMode: Boolean = false,

    // Diagnostics & Gestures
    val isNerdStatsVisible: Boolean = false,
    val nerdStats: NerdStatsInfo? = null,
    val isFastPlaying: Boolean = false,
    val fastPlaySpeed: Float = 2.0f,
    val isSidebarOpen: Boolean = false,
    val sidebarActiveSubView: String = "main",
    val isQueueDrawerOpen: Boolean = false,

    // Playlist Queue & Error Status
    val playlist: List<VideoItem> = emptyList(),
    val currentPlaylistIndex: Int = -1,
    val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
class PlayMePlayerController(
    private val context: Context,
    private val repository: com.playme.player.core.repository.VideoRepository,
    private val settingsManager: com.playme.player.core.settings.SettingsManager
) {

    companion object {
        val EQ_PRESETS = mapOf(
            "Flat" to listOf(0, 0, 0, 0, 0),
            "Rock" to listOf(5, 3, -1, 2, 4),
            "Pop" to listOf(-1, 1, 3, 2, -1),
            "Dance" to listOf(5, 3, -1, 2, 4),
            "Bass Boost" to listOf(6, 4, 1, 0, 0),
            "Vocal" to listOf(-2, 1, 4, 3, 0)
        )
    }

    private val trackSelector = DefaultTrackSelector(context)

    var exoPlayer: ExoPlayer = buildPlayer(DecoderMode.HW)
        private set

    private val onPlayerRebuiltListeners = mutableListOf<(ExoPlayer) -> Unit>()

    fun addOnPlayerRebuiltListener(listener: (ExoPlayer) -> Unit) {
        onPlayerRebuiltListeners.add(listener)
    }

    fun removeOnPlayerRebuiltListener(listener: (ExoPlayer) -> Unit) {
        onPlayerRebuiltListeners.remove(listener)
    }

    // Hardware Audio Effects
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressTrackerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var initialSleepMinutes: Int = 0
    private var preFastPlaySpeed: Float = 1.0f

    init {
        setupDefaultFallbackBands()
        setupPlayerListeners()
    }

    private fun buildPlayer(decoderMode: DecoderMode): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableDecoderFallback(true)

            setMediaCodecSelector(object : MediaCodecSelector {
                override fun getDecoderInfos(
                    mimeType: String,
                    requiresSecureDecoder: Boolean,
                    requiresTunnelingDecoder: Boolean
                ): MutableList<MediaCodecInfo> {
                    val infos = MediaCodecUtil.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder)
                    return if (decoderMode == DecoderMode.SW) {
                        val swOnly = infos.filter { !it.hardwareAccelerated }
                        if (swOnly.isNotEmpty()) swOnly.toMutableList() else infos.toMutableList()
                    } else {
                        infos.toMutableList()
                    }
                }
            })
        }

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 50_000,
                /* bufferForPlaybackMs = */ 250,
                /* bufferForPlaybackAfterRebufferMs = */ 500
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        return ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                setSeekParameters(SeekParameters.CLOSEST_SYNC)
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
    }

    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                    startPlaybackService()
                } else {
                    stopProgressTracker()
                    saveCurrentPlaybackProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val duration = if (playbackState == Player.STATE_READY) exoPlayer.duration.coerceAtLeast(0L) else _uiState.value.durationMs
                _uiState.value = _uiState.value.copy(
                    isBuffering = isBuffering,
                    durationMs = duration
                )

                if (playbackState == Player.STATE_READY) {
                    initAudioEffects()
                    if (_uiState.value.isNerdStatsVisible) {
                        updateNerdStats()
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    // Check if sleep timer was waiting for active video to finish and has actually expired
                    val config = _uiState.value.sleepTimerConfig
                    val remainingSec = _uiState.value.sleepTimerRemainingSec
                    if (config?.waitForMediaEnd == true && remainingSec == 0L) {
                        exoPlayer.pause()
                        _uiState.value = _uiState.value.copy(
                            sleepTimerConfig = null,
                            sleepTimerRemainingSec = null
                        )
                    } else if (hasNextVideo()) {
                        playNext()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                saveCurrentPlaybackProgress()
                val mediaId = mediaItem?.mediaId
                val index = _uiState.value.playlist.indexOfFirst { it.id.toString() == mediaId }
                if (index != -1) {
                    val nextVideo = _uiState.value.playlist[index]
                    _uiState.value = _uiState.value.copy(
                        currentPlaylistIndex = index,
                        currentVideo = nextVideo
                    )
                    updateCurrentMediaMetadata(nextVideo)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = error.localizedMessage ?: "Playback error"
                _uiState.value = _uiState.value.copy(
                    isBuffering = false,
                    errorMessage = errorMsg
                )
                if (_uiState.value.decoderMode == DecoderMode.HW) {
                    setDecoderMode(DecoderMode.SW)
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateTracks(tracks)
                if (_uiState.value.isNerdStatsVisible) {
                    updateNerdStats()
                }
            }
        })
    }

    private fun initAudioEffects() {
        try {
            val audioSessionId = exoPlayer.audioSessionId
            if (audioSessionId == C.AUDIO_SESSION_ID_UNSET) return

            if (equalizer == null) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = _uiState.value.equalizerEnabled
                }
                updateEqualizerBandsFromNative()
            }
            if (bassBoost == null) {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = true
                    setStrength((_uiState.value.bassBoostStrength).toShort())
                }
            }
            if (virtualizer == null) {
                virtualizer = Virtualizer(0, audioSessionId).apply {
                    enabled = true
                    setStrength((_uiState.value.virtualizerStrength).toShort())
                }
            }
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = true
                }
                setVolumeBoost(_uiState.value.volumeBoostPercent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateEqualizerBandsFromNative() {
        val eq = equalizer
        if (eq != null) {
            try {
                val numBands = eq.numberOfBands.toInt()
                val minMilliBels = eq.bandLevelRange?.getOrNull(0)?.toInt() ?: -1200
                val maxMilliBels = eq.bandLevelRange?.getOrNull(1)?.toInt() ?: 1200
                val bands = (0 until numBands).map { bandIdx ->
                    EqualizerBand(
                        index = bandIdx,
                        centerFreqHz = eq.getCenterFreq(bandIdx.toShort()) / 1000,
                        minMilliBels = minMilliBels,
                        maxMilliBels = maxMilliBels,
                        levelMilliBels = eq.getBandLevel(bandIdx.toShort()).toInt()
                    )
                }
                if (bands.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(equalizerBands = bands)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        setupDefaultFallbackBands()
    }

    private fun setupDefaultFallbackBands() {
        val defaultFreqs = listOf(60, 230, 910, 3600, 14000)
        val bands = defaultFreqs.mapIndexed { idx, freq ->
            EqualizerBand(
                index = idx,
                centerFreqHz = freq,
                minMilliBels = -1200,
                maxMilliBels = 1200,
                levelMilliBels = 0
            )
        }
        _uiState.value = _uiState.value.copy(equalizerBands = bands)
    }

    fun playQueue(videos: List<VideoItem>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        if (videos.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, videos.size - 1)
        val current = videos[safeIndex]

        // Auto-resume from last saved position if not near the end
        val resolvedPosition = if (startPositionMs > 0L) {
            startPositionMs
        } else if (current.lastPlayedPositionMs > 2000L && (current.durationMs <= 0 || current.durationMs - current.lastPlayedPositionMs > 5000L)) {
            current.lastPlayedPositionMs
        } else {
            0L
        }

        _uiState.value = _uiState.value.copy(
            playlist = videos,
            currentPlaylistIndex = safeIndex,
            currentVideo = current,
            errorMessage = null
        )

        val mediaItems = videos.map { video ->
            val metadata = MediaMetadata.Builder()
                .setTitle(video.displayName)
                .setDisplayTitle(video.displayName)
                .setArtist(if (video.folderName.isNotBlank()) video.folderName else "PlayMe Player")
                .build()

            MediaItem.Builder()
                .setUri(video.uri)
                .setMediaId(video.id.toString())
                .setMediaMetadata(metadata)
                .build()
        }

        val effectiveSpeed = settingsManager.getEffectiveSpeed(current.folderPath)
        exoPlayer.setPlaybackSpeed(effectiveSpeed)
        _uiState.value = _uiState.value.copy(playbackSpeed = effectiveSpeed)

        exoPlayer.setMediaItems(mediaItems, safeIndex, resolvedPosition)
        exoPlayer.prepare()
        exoPlayer.play()
        if (current != null) {
            updateCurrentMediaMetadata(current)
        }
        startPlaybackService()
    }

    fun playVideo(video: VideoItem, startPositionMs: Long = 0L) {
        val existingIndex = _uiState.value.playlist.indexOfFirst { it.id == video.id }
        if (existingIndex != -1 && _uiState.value.playlist.isNotEmpty()) {
            skipToQueueIndex(existingIndex, startPositionMs)
        } else {
            playQueue(listOf(video), startIndex = 0, startPositionMs = startPositionMs)
        }
    }

    fun playUri(uri: Uri, displayName: String?, startPositionMs: Long = 0L) {
        val name = displayName ?: (uri.lastPathSegment ?: "Media Playback")
        val video = VideoItem(
            id = uri.hashCode().toLong(),
            title = name,
            displayName = name,
            path = uri.path ?: "",
            contentUri = uri.toString(),
            durationMs = 0L,
            sizeBytes = 0L,
            dateAdded = System.currentTimeMillis(),
            width = 1920,
            height = 1080,
            folderName = "External",
            folderPath = ""
        )
        playQueue(listOf(video), startIndex = 0, startPositionMs = startPositionMs)
    }

    fun playVaultItem(vaultItem: VaultItemEntity) {
        val uri = Uri.fromFile(java.io.File(vaultItem.vaultPath))
        val video = VideoItem(
            id = vaultItem.id.toLong(),
            title = vaultItem.title,
            displayName = vaultItem.title,
            path = vaultItem.vaultPath,
            contentUri = uri.toString(),
            durationMs = vaultItem.durationMs,
            sizeBytes = vaultItem.sizeBytes,
            dateAdded = vaultItem.addedTimestamp,
            width = 1920,
            height = 1080,
            folderName = "Private Vault",
            folderPath = ""
        )

        _uiState.value = _uiState.value.copy(
            playlist = listOf(video),
            currentPlaylistIndex = 0,
            currentVideo = video,
            errorMessage = null
        )

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(vaultItem.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(vaultItem.title)
                    .setDisplayTitle(vaultItem.title)
                    .setArtist("Private Vault")
                    .build()
            )
            .build()

        val mediaSource = ProgressiveMediaSource.Factory(XorDataSource.Factory(context))
            .createMediaSource(mediaItem)

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
        updateCurrentMediaMetadata(video)
        startPlaybackService()
    }

    fun hasNextVideo(): Boolean {
        val count = _uiState.value.playlist.size
        val cur = _uiState.value.currentPlaylistIndex
        return cur in 0 until (count - 1)
    }

    fun hasPreviousVideo(): Boolean {
        return _uiState.value.currentPlaylistIndex > 0
    }

    fun playNext() {
        if (hasNextVideo()) {
            val nextIndex = _uiState.value.currentPlaylistIndex + 1
            skipToQueueIndex(nextIndex)
        }
    }

    fun playPrevious() {
        if (hasPreviousVideo()) {
            val prevIndex = _uiState.value.currentPlaylistIndex - 1
            skipToQueueIndex(prevIndex)
        } else {
            seekTo(0L)
        }
    }

    fun skipToQueueIndex(index: Int, startPositionMs: Long = 0L) {
        val playlist = _uiState.value.playlist
        if (index in playlist.indices) {
            val nextVideo = playlist[index]
            _uiState.value = _uiState.value.copy(
                currentPlaylistIndex = index,
                currentVideo = nextVideo,
                errorMessage = null
            )
            exoPlayer.seekTo(index, startPositionMs)
            exoPlayer.play()
        }
    }

    fun reorderPlaylist(fromIndex: Int, toIndex: Int) {
        val currentList = _uiState.value.playlist.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            val currentPlayingId = _uiState.value.currentVideo?.id
            val newCurrentIndex = currentList.indexOfFirst { it.id == currentPlayingId }

            _uiState.value = _uiState.value.copy(
                playlist = currentList,
                currentPlaylistIndex = newCurrentIndex
            )
            try {
                exoPlayer.moveMediaItem(fromIndex, toIndex)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeFromPlaylist(index: Int) {
        val currentList = _uiState.value.playlist.toMutableList()
        if (index in currentList.indices && currentList.size > 1) {
            currentList.removeAt(index)
            val currentPlayingId = _uiState.value.currentVideo?.id
            val newCurrentIndex = currentList.indexOfFirst { it.id == currentPlayingId }

            _uiState.value = _uiState.value.copy(
                playlist = currentList,
                currentPlaylistIndex = newCurrentIndex.coerceAtLeast(0)
            )
            try {
                exoPlayer.removeMediaItem(index)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleMute() {
        val nextMute = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = nextMute)
        exoPlayer.volume = if (nextMute) 0f else 1f
    }

    fun togglePlayPause() {
        onUserInteraction()
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        onUserInteraction()
        try {
            val dur = exoPlayer.duration
            val maxPos = if (dur > 0L) dur else Long.MAX_VALUE
            val bounded = positionMs.coerceIn(0L, maxPos)
            exoPlayer.seekTo(bounded)
            _uiState.value = _uiState.value.copy(currentPositionMs = bounded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekBy(offsetMs: Long) {
        onUserInteraction()
        try {
            val current = exoPlayer.currentPosition.coerceAtLeast(0L)
            seekTo(current + offsetMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        onUserInteraction()
        exoPlayer.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun cycleAspectRatio() {
        onUserInteraction()
        val nextMode = when (_uiState.value.aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioMode.ZOOM
            AspectRatioMode.ZOOM -> AspectRatioMode.STRETCH
            AspectRatioMode.STRETCH -> AspectRatioMode.RATIO_16_9
            AspectRatioMode.RATIO_16_9 -> AspectRatioMode.RATIO_4_3
            AspectRatioMode.RATIO_4_3 -> AspectRatioMode.FIT
        }
        _uiState.value = _uiState.value.copy(aspectRatioMode = nextMode)
    }

    fun setAspectRatioMode(mode: AspectRatioMode) {
        onUserInteraction()
        _uiState.value = _uiState.value.copy(aspectRatioMode = mode)
    }

    fun setVolumeBoost(boostPercent: Int) {
        val clamped = boostPercent.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(volumeBoostPercent = clamped)
        try {
            // LoudnessEnhancer takes target gain in millibels (mB): 0 to 2000 mB (+20 dB)
            val gainMb = (clamped * 20)
            loudnessEnhancer?.setTargetGain(gainMb)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleScreenLock() {
        _uiState.value = _uiState.value.copy(isScreenLocked = !_uiState.value.isScreenLocked)
    }

    fun toggleBackgroundPlay() {
        val enabled = !_uiState.value.isBackgroundPlayEnabled
        _uiState.value = _uiState.value.copy(isBackgroundPlayEnabled = enabled)
        if (enabled) {
            startPlaybackService()
        }
    }

    fun setDecoderMode(mode: DecoderMode) {
        if (_uiState.value.decoderMode == mode) return

        val currentPlaylist = _uiState.value.playlist
        val currentIdx = _uiState.value.currentPlaylistIndex
        val currentPos = exoPlayer.currentPosition
        val isPlaying = exoPlayer.isPlaying
        val curSpeed = _uiState.value.playbackSpeed
        val currentEqPreset = _uiState.value.currentEqPreset
        val currentBands = _uiState.value.equalizerBands
        val currentBass = _uiState.value.bassBoostStrength
        val currentVirtualizer = _uiState.value.virtualizerStrength
        val currentBoost = _uiState.value.volumeBoostPercent
        val isMuted = _uiState.value.isMuted

        _uiState.value = _uiState.value.copy(decoderMode = mode)

        // Release old player & audio fx
        releaseAudioEffects()
        exoPlayer.release()

        // Build new player
        exoPlayer = buildPlayer(mode)
        exoPlayer.setPlaybackSpeed(curSpeed)
        exoPlayer.volume = if (isMuted) 0f else 1f
        setupPlayerListeners()

        // Notify active services (like PlaybackService) that ExoPlayer was rebuilt!
        onPlayerRebuiltListeners.forEach { it(exoPlayer) }

        // Restore entire playlist queue
        if (currentPlaylist.isNotEmpty()) {
            val mediaItems = currentPlaylist.map { video ->
                val metadata = MediaMetadata.Builder()
                    .setTitle(video.displayName)
                    .setDisplayTitle(video.displayName)
                    .setArtist(if (video.folderName.isNotBlank()) video.folderName else "PlayMe Player")
                    .build()

                MediaItem.Builder()
                    .setUri(video.uri)
                    .setMediaId(video.id.toString())
                    .setMediaMetadata(metadata)
                    .build()
            }
            val safeIdx = currentIdx.coerceIn(0, mediaItems.size - 1)
            exoPlayer.setMediaItems(mediaItems, safeIdx, currentPos)
            exoPlayer.prepare()
            if (isPlaying) {
                exoPlayer.play()
            }
            currentPlaylist.getOrNull(safeIdx)?.let { updateCurrentMediaMetadata(it) }
        }

        // Restore audio effects
        initAudioEffects()
        setEqualizerPreset(currentEqPreset)
        currentBands.forEach { band ->
            setEqualizerBandLevel(band.index, band.levelMilliBels)
        }
        setBassBoost(currentBass / 10)
        setVirtualizer(currentVirtualizer / 10)
        setVolumeBoost(currentBoost)
    }

    // ==========================================
    // EQUALIZER & AUDIO EFFECTS CONTROLS
    // ==========================================

    fun setEqualizerEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(equalizerEnabled = enabled)
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerBandLevel(bandIndex: Int, levelMilliBels: Int) {
        val updatedBands = _uiState.value.equalizerBands.map { band ->
            if (band.index == bandIndex) band.copy(levelMilliBels = levelMilliBels) else band
        }
        _uiState.value = _uiState.value.copy(equalizerBands = updatedBands)
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), levelMilliBels.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerPreset(presetName: String) {
        val levels = EQ_PRESETS[presetName] ?: return
        _uiState.value = _uiState.value.copy(currentEqPreset = presetName)
        levels.forEachIndexed { idx, dB ->
            setEqualizerBandLevel(idx, dB * 100)
        }
    }

    fun setBassBoost(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        val strength = (clamped * 10).toShort()
        _uiState.value = _uiState.value.copy(bassBoostStrength = strength.toInt())
        try {
            bassBoost?.setStrength(strength)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVirtualizer(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        val strength = (clamped * 10).toShort()
        _uiState.value = _uiState.value.copy(virtualizerStrength = strength.toInt())
        try {
            virtualizer?.setStrength(strength)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleQuickAudioBoost() {
        val next = !_uiState.value.isQuickAudioBoostActive
        _uiState.value = _uiState.value.copy(isQuickAudioBoostActive = next)
        setVolumeBoost(if (next) 100 else 0)
    }

    // ==========================================
    // DELAY SYNC & BLUETOOTH
    // ==========================================

    fun setAudioDelay(delayMs: Long) {
        val clamped = delayMs.coerceIn(-500L, 500L)
        _uiState.value = _uiState.value.copy(audioDelayMs = clamped)
    }

    fun setSubtitleDelay(delayMs: Long) {
        val clamped = delayMs.coerceIn(-500L, 500L)
        _uiState.value = _uiState.value.copy(subtitleDelayMs = clamped)
    }

    fun toggleBluetoothDelay() {
        val next = !_uiState.value.isBluetoothDelayActive
        val delay = if (next) 180L else 0L
        _uiState.value = _uiState.value.copy(
            isBluetoothDelayActive = next,
            audioDelayMs = delay
        )
    }

    // ==========================================
    // A-B REPEAT LOOPING
    // ==========================================

    fun toggleABRepeat() {
        val current = _uiState.value.abRepeat
        val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
        val next = when {
            !current.isEnabled || current.pointA == null -> {
                ABRepeatState(isEnabled = true, pointA = pos, pointB = null)
            }
            current.pointB == null -> {
                val ptB = if (pos > current.pointA) pos else current.pointA + 2000L
                current.copy(pointB = ptB)
            }
            else -> {
                ABRepeatState(isEnabled = false, pointA = null, pointB = null)
            }
        }
        _uiState.value = _uiState.value.copy(abRepeat = next)
    }

    // ==========================================
    // SMART SLEEP TIMER
    // ==========================================

    fun setSleepTimer(minutes: Int, waitForMediaEnd: Boolean = true, resetOnInteraction: Boolean = true) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        initialSleepMinutes = minutes

        if (minutes == 0) {
            _uiState.value = _uiState.value.copy(
                sleepTimerConfig = null,
                sleepTimerRemainingSec = null
            )
            return
        }

        val config = SleepTimerConfig(
            minutes = minutes,
            waitForMediaEnd = waitForMediaEnd,
            resetOnInteraction = resetOnInteraction
        )

        if (minutes == -1) {
            _uiState.value = _uiState.value.copy(
                sleepTimerConfig = config,
                sleepTimerRemainingSec = null
            )
            return
        }

        val totalSec = minutes * 60L
        _uiState.value = _uiState.value.copy(
            sleepTimerConfig = config,
            sleepTimerRemainingSec = totalSec
        )

        sleepTimerJob = scope.launch {
            var remaining = totalSec
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(sleepTimerRemainingSec = remaining)
            }
            if (isActive) {
                triggerSleepTimerExpiry()
            }
        }
    }

    fun onUserInteraction() {
        val config = _uiState.value.sleepTimerConfig ?: return
        if (config.resetOnInteraction && initialSleepMinutes > 0) {
            setSleepTimer(initialSleepMinutes, config.waitForMediaEnd, config.resetOnInteraction)
        }
    }

    private fun triggerSleepTimerExpiry() {
        val config = _uiState.value.sleepTimerConfig
        if (config?.waitForMediaEnd == true && exoPlayer.isPlaying) {
            _uiState.value = _uiState.value.copy(sleepTimerRemainingSec = 0L)
        } else {
            exoPlayer.pause()
            _uiState.value = _uiState.value.copy(
                sleepTimerConfig = null,
                sleepTimerRemainingSec = null
            )
        }
    }

    // ==========================================
    // HOLD-TO-FASTPLAY INTERACTIVE SLIDER
    // ==========================================

    private var defaultHoldSpeed: Float = 2.0f
    private var wasPlayingBeforeFastPlay: Boolean = false

    fun startFastPlay() {
        preFastPlaySpeed = _uiState.value.playbackSpeed
        wasPlayingBeforeFastPlay = exoPlayer.isPlaying
        val targetSpeed = defaultHoldSpeed
        exoPlayer.setPlaybackSpeed(targetSpeed)
        if (!wasPlayingBeforeFastPlay) {
            exoPlayer.play()
        }
        _uiState.value = _uiState.value.copy(
            isFastPlaying = true,
            fastPlaySpeed = targetSpeed,
            playbackSpeed = targetSpeed,
            isPlaying = true
        )
    }

    fun setFastPlaySpeed(speed: Float) {
        val clamped = ((speed * 10).toInt() / 10f).coerceIn(0.1f, 4.0f)
        defaultHoldSpeed = clamped
        exoPlayer.setPlaybackSpeed(clamped)
        _uiState.value = _uiState.value.copy(
            fastPlaySpeed = clamped,
            playbackSpeed = clamped
        )
    }

    fun stopFastPlay() {
        exoPlayer.setPlaybackSpeed(preFastPlaySpeed)
        if (!wasPlayingBeforeFastPlay) {
            exoPlayer.pause()
        }
        _uiState.value = _uiState.value.copy(
            isFastPlaying = false,
            playbackSpeed = preFastPlaySpeed,
            isPlaying = wasPlayingBeforeFastPlay
        )
    }

    // ==========================================
    // VIDEO DISPLAY MODES & NERD STATS
    // ==========================================

    fun toggleMirrorMode() {
        _uiState.value = _uiState.value.copy(isMirrorMode = !_uiState.value.isMirrorMode)
    }

    fun toggleNightMode() {
        _uiState.value = _uiState.value.copy(isNightMode = !_uiState.value.isNightMode)
    }

    fun toggleNerdStats() {
        val next = !_uiState.value.isNerdStatsVisible
        _uiState.value = _uiState.value.copy(isNerdStatsVisible = next)
        if (next) updateNerdStats()
    }

    private fun updateNerdStats() {
        val videoFormat = exoPlayer.videoFormat
        val audioFormat = exoPlayer.audioFormat
        val res = if (videoFormat != null) "${videoFormat.width}x${videoFormat.height}" else "1920x1080"
        val vCodec = videoFormat?.sampleMimeType ?: "video/avc"
        val aCodec = audioFormat?.sampleMimeType ?: "audio/mp4a-latm"
        val fps = videoFormat?.frameRate?.takeIf { it > 0 } ?: 24.0f
        val channels = audioFormat?.channelCount ?: 2
        val sampleRate = audioFormat?.sampleRate ?: 48000
        val isHw = _uiState.value.decoderMode == DecoderMode.HW

        val dropped = exoPlayer.videoDecoderCounters?.droppedBufferCount ?: 0

        _uiState.value = _uiState.value.copy(
            nerdStats = NerdStatsInfo(
                resolution = res,
                videoCodec = vCodec,
                decoderName = if (isHw) "c2.android.hardware.decoder (HW)" else "c2.google.software.decoder (SW)",
                isHardwareAccelerated = isHw,
                bitrateKbps = (videoFormat?.bitrate?.takeIf { it > 0 } ?: 4500000) / 1000L,
                frameRate = fps,
                droppedFrames = dropped,
                audioCodec = aCodec,
                audioChannels = channels,
                sampleRateHz = sampleRate,
                bufferDurationMs = (_uiState.value.bufferedPositionMs - _uiState.value.currentPositionMs).coerceAtLeast(0L)
            )
        )
    }

    // ==========================================
    // SIDEBAR & QUEUE DRAWER CONTROLS
    // ==========================================

    fun startPlaybackService() {
        try {
            val intent = Intent(context, PlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCurrentMediaMetadata(video: VideoItem) {
        scope.launch(Dispatchers.IO) {
            val thumbnailBytes = try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(video.uri, android.util.Size(512, 512), null)
                } else {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(context, video.uri)
                    val bm = retriever.getFrameAtTime(1000000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    retriever.release()
                    bm
                }
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    stream.toByteArray()
                } else null
            } catch (e: Exception) {
                null
            }

            withContext(Dispatchers.Main) {
                if (_uiState.value.currentVideo?.id == video.id) {
                    val metadata = MediaMetadata.Builder()
                        .setTitle(video.displayName)
                        .setDisplayTitle(video.displayName)
                        .setArtist(if (video.folderName.isNotBlank()) video.folderName else "PlayMe Player")
                        .setAlbumTitle("PlayMe")
                        .apply {
                            if (thumbnailBytes != null) {
                                setArtworkData(thumbnailBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                            }
                        }
                        .build()

                    val currentIdx = exoPlayer.currentMediaItemIndex
                    val currentItem = exoPlayer.currentMediaItem
                    if (currentItem != null && currentIdx >= 0) {
                        val updatedItem = currentItem.buildUpon()
                            .setMediaMetadata(metadata)
                            .build()
                        try {
                            exoPlayer.replaceMediaItem(currentIdx, updatedItem)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    exoPlayer.playlistMetadata = metadata
                }
            }
        }
    }

    fun detachVideoSurface() {
        try {
            exoPlayer.clearVideoSurface()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openSidebar(subView: String = "main") {
        onUserInteraction()
        _uiState.value = _uiState.value.copy(
            isSidebarOpen = true,
            sidebarActiveSubView = subView,
            isQueueDrawerOpen = false
        )
    }

    fun closeSidebar() {
        _uiState.value = _uiState.value.copy(
            isSidebarOpen = false,
            isQueueDrawerOpen = false
        )
    }

    fun openQueueDrawer() {
        onUserInteraction()
        _uiState.value = _uiState.value.copy(
            isQueueDrawerOpen = true,
            isSidebarOpen = false
        )
    }

    fun closeQueueDrawer() {
        _uiState.value = _uiState.value.copy(isQueueDrawerOpen = false)
    }

    fun toggleQueueDrawer() {
        onUserInteraction()
        _uiState.value = _uiState.value.copy(
            isQueueDrawerOpen = !_uiState.value.isQueueDrawerOpen,
            isSidebarOpen = false
        )
    }

    fun setSidebarSubView(subView: String) {
        _uiState.value = _uiState.value.copy(sidebarActiveSubView = subView)
    }

    // ==========================================
    // TRACK SELECTION & SUBTITLES
    // ==========================================

    fun selectAudioTrack(trackId: String) {
        val tracks = exoPlayer.currentTracks
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                val trackGroup = group.mediaTrackGroup
                for (i in 0 until trackGroup.length) {
                    val id = "${trackGroup.id}_$i"
                    if (id == trackId) {
                        trackSelector.parameters = trackSelector.buildUponParameters()
                            .setOverrideForType(TrackSelectionOverride(trackGroup, i))
                            .build()
                        updateTracks(tracks)
                        return
                    }
                }
            }
        }
    }

    fun selectSubtitleTrack(trackId: String?) {
        if (trackId == null) {
            trackSelector.parameters = trackSelector.buildUponParameters()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
        } else {
            val tracks = exoPlayer.currentTracks
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_TEXT) {
                    val trackGroup = group.mediaTrackGroup
                    for (i in 0 until trackGroup.length) {
                        val id = "${trackGroup.id}_$i"
                        if (id == trackId) {
                            trackSelector.parameters = trackSelector.buildUponParameters()
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .setOverrideForType(TrackSelectionOverride(trackGroup, i))
                                .build()
                            updateTracks(tracks)
                            return
                        }
                    }
                }
            }
        }
    }

    fun loadExternalSubtitle(uri: Uri, mimeType: String = MimeTypes.APPLICATION_SUBRIP) {
        val subConfig = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mimeType)
            .setLanguage("und")
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()

        val currentItem = exoPlayer.currentMediaItem ?: return
        val updated = currentItem.buildUpon()
            .setSubtitleConfigurations(listOf(subConfig))
            .build()

        val currentIdx = exoPlayer.currentMediaItemIndex
        if (currentIdx in _uiState.value.playlist.indices) {
            exoPlayer.replaceMediaItem(currentIdx, updated)
        }
    }

    private fun updateTracks(tracks: Tracks) {
        val subtitles = mutableListOf<SubtitleTrackItem>()
        val audioTracks = mutableListOf<AudioTrackItem>()

        for (group in tracks.groups) {
            val trackType = group.type
            val trackGroup = group.mediaTrackGroup

            if (trackType == C.TRACK_TYPE_TEXT) {
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(i)
                    val isSelected = group.isTrackSelected(i)
                    val label = format.label ?: format.language ?: "Subtitle ${subtitles.size + 1}"
                    subtitles.add(
                        SubtitleTrackItem(
                            id = "${trackGroup.id}_$i",
                            name = label,
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                }
            } else if (trackType == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(i)
                    val isSelected = group.isTrackSelected(i)
                    val label = format.label ?: format.language ?: "Track ${audioTracks.size + 1} (${if (format.channelCount > 2) "5.1 Surround" else "Stereo"})"
                    val codecLabel = when {
                        format.sampleMimeType?.contains("ac3", ignoreCase = true) == true -> "AC3"
                        format.sampleMimeType?.contains("eac3", ignoreCase = true) == true -> "E-AC3"
                        format.sampleMimeType?.contains("opus", ignoreCase = true) == true -> "Opus"
                        format.sampleMimeType?.contains("flac", ignoreCase = true) == true -> "FLAC"
                        else -> "AAC"
                    }
                    val bitrateStr = if (format.bitrate > 0) "${format.bitrate / 1000} kbps" else "128 kbps"
                    audioTracks.add(
                        AudioTrackItem(
                            id = "${trackGroup.id}_$i",
                            name = label,
                            language = format.language,
                            channels = format.channelCount,
                            bitrate = bitrateStr,
                            codec = codecLabel,
                            isHardwareAccelerated = _uiState.value.decoderMode == DecoderMode.HW,
                            isSelected = isSelected
                        )
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            availableSubtitles = subtitles,
            availableAudioTracks = audioTracks,
            currentSubtitle = subtitles.firstOrNull { it.isSelected },
            currentAudioTrack = audioTracks.firstOrNull { it.isSelected }
        )

        // Auto-select preferred audio track if not default
        val prefAudio = settingsManager.getPreferredAudioLanguage()
        if (prefAudio != "default" && audioTracks.isNotEmpty()) {
            val matched = audioTracks.firstOrNull { it.language?.equals(prefAudio, ignoreCase = true) == true }
            if (matched != null && !matched.isSelected) {
                selectAudioTrack(matched.id)
            }
        }

        // Auto-select preferred subtitle track or disable if off
        val prefSub = settingsManager.getPreferredSubtitleLanguage()
        if (prefSub == "off") {
            if (_uiState.value.currentSubtitle != null) {
                selectSubtitleTrack(null)
            }
        } else if (prefSub != "auto" && subtitles.isNotEmpty()) {
            val matchedSub = subtitles.firstOrNull { it.language?.equals(prefSub, ignoreCase = true) == true }
            if (matchedSub != null && !matchedSub.isSelected) {
                selectSubtitleTrack(matchedSub.id)
            }
        }
    }

    fun saveCurrentPlaybackProgress() {
        val curVideo = _uiState.value.currentVideo ?: return
        val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
        val dur = exoPlayer.duration.coerceAtLeast(0L)
        if (pos > 0L) {
            scope.launch(Dispatchers.IO) {
                try {
                    repository.saveProgress(
                        uri = curVideo.contentUri,
                        title = curVideo.displayName,
                        positionMs = pos,
                        durationMs = dur
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = scope.launch {
            var isSeekingAB = false
            var tickCount = 0
            while (isActive) {
                val curPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                val bufPos = exoPlayer.bufferedPosition.coerceAtLeast(0L)

                // A-B repeat check with asynchronous seek debounce guard
                val ab = _uiState.value.abRepeat
                if (ab.isEnabled && ab.pointA != null && ab.pointB != null) {
                    if (curPos >= ab.pointB && !isSeekingAB) {
                        isSeekingAB = true
                        exoPlayer.seekTo(ab.pointA)
                        _uiState.value = _uiState.value.copy(currentPositionMs = ab.pointA)
                    } else if (curPos < ab.pointB) {
                        isSeekingAB = false
                    }
                }

                _uiState.value = _uiState.value.copy(
                    currentPositionMs = curPos,
                    bufferedPositionMs = bufPos
                )

                // Persist progress to database every 2 seconds (8 ticks * 250ms)
                tickCount++
                if (tickCount % 8 == 0) {
                    saveCurrentPlaybackProgress()
                }

                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    private fun releaseAudioEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }

    fun release() {
        saveCurrentPlaybackProgress()
        stopProgressTracker()
        sleepTimerJob?.cancel()
        releaseAudioEffects()
        exoPlayer.release()
    }
}
