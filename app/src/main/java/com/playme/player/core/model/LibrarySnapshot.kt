package com.playme.player.core.model

/**
 * Which stage of the scan produced a snapshot.
 *
 * The library is published progressively so the user sees content immediately
 * instead of staring at a spinner while the deep storage walk runs.
 */
enum class LibraryPhase {
    /** Restored from the on-disk snapshot of the previous run. */
    CACHE,

    /** MediaStore query finished — the fast, indexed source. */
    MEDIA_STORE,

    /** A partial batch from the recursive storage walk. */
    DEEP_SCAN,

    /** Everything is scanned and persisted. */
    COMPLETE
}

data class LibrarySnapshot(
    val videos: List<VideoItem>,
    val phase: LibraryPhase,
    val series: List<SeriesItem> = emptyList()
)
