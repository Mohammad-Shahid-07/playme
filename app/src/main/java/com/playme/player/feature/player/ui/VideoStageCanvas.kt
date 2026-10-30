package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.library.ui.VideoThumbnailImage
import com.playme.player.feature.player.model.AspectRatioMode

@Composable
fun VideoStageCanvas(
    video: VideoItem?,
    aspectRatioMode: AspectRatioMode,
    isControlsVisible: Boolean,
    currentSubtitle: String?,
    modifier: Modifier = Modifier,
    videoSurface: @Composable (() -> Unit)? = null
) {
    val contentScale = when (aspectRatioMode) {
        AspectRatioMode.FIT_SCREEN, AspectRatioMode.FILL -> ContentScale.Crop
        else -> ContentScale.Fit
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05060A))
    ) {
        // 1. Dynamic Atmospheric Ambilight Glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.85f)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PlayMeColors.ElectricViolet.copy(alpha = 0.22f),
                            PlayMeColors.DeepViolet.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. Cached poster frame. Shown instantly on open so the transition
        //    never feels like a black wait while codecs spin up.
        if (video != null) {
            VideoThumbnailImage(
                video = video,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Video Surface (transparent until the first decoded frame)
        videoSurface?.invoke()

        // 4. Top & Bottom Cinematic Gradient Scrims (Fade with controls)
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2000000),
                                Color(0x99000000),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x99000000),
                                Color(0xF5000000)
                            )
                        )
                    )
            )
        }

        // 5. Live On-Screen Subtitle Pill
        if (!currentSubtitle.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isControlsVisible) 110.dp else 44.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xDD0C0E18))
                    .border(1.dp, Color(0x33757BF8), RoundedCornerShape(999.dp))
                    .padding(horizontal = 18.dp, vertical = 7.dp)
            ) {
                Text(
                    text = currentSubtitle,
                    color = Color(0xFFF8FAFC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}
