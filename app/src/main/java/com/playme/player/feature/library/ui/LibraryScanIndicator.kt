package com.playme.player.feature.library.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A 2dp hairline that occupies its slot permanently, so the content below
 * never shifts when a background scan starts or finishes.
 */
@Composable
fun LibraryScanIndicator(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Color(0xFF6366F1),
                trackColor = Color(0x14FFFFFF)
            )
        }
    }
}
