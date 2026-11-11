package com.playme.player.feature.library.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * True while the enclosing list or pager is being dragged or flung.
 *
 * Thumbnail decoding is deferred until this is false, so a fling never
 * competes with the frame pipeline. A CompositionLocal keeps that signal
 * scoped to the composition instead of parking UI state in a singleton.
 */
val LocalListScrolling = compositionLocalOf { false }
