package com.playme.player.feature.player.vm

object PlayerDelaysDelegate {
    const val STEP_MS = 50L
    private const val MIN_DELAY_MS = -5000L
    private const val MAX_DELAY_MS = 5000L

    fun stepAudio(current: Long, deltaMs: Long): Long =
        (current + deltaMs).coerceIn(MIN_DELAY_MS, MAX_DELAY_MS)

    fun stepSubtitle(current: Long, deltaMs: Long): Long =
        (current + deltaMs).coerceIn(MIN_DELAY_MS, MAX_DELAY_MS)
}
