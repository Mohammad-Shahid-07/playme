package com.playme.player.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.playme.player.core.designsystem.PlayMeDesign
import com.playme.player.core.designsystem.darkGlassCard
import com.playme.player.core.model.VideoFolder
import com.playme.player.core.model.VideoItem
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Obsidian Violet Glass Video Card supporting both List Mode and PlayIt-style 2-Column Grid Mode.
 */
@Composable
fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onMoveToVault: () -> Unit,
    modifier: Modifier = Modifier,
    isGridMode: Boolean = false,
    isLastPlayed: Boolean = false
) {
    if (isGridMode) {
        VideoCardGrid(
            video = video,
            onClick = onClick,
            onMoveToVault = onMoveToVault,
            modifier = modifier,
            isLastPlayed = isLastPlayed
        )
    } else {
        VideoCardList(
            video = video,
            onClick = onClick,
            onMoveToVault = onMoveToVault,
            modifier = modifier,
            isLastPlayed = isLastPlayed
        )
    }
}

/**
 * List Mode Video Card: Matches Screenshot 1 with New pill badge, Folder · Remaining time, and clean metadata specs.
 */
@Composable
fun VideoCardList(
    video: VideoItem,
    onClick: () -> Unit,
    onMoveToVault: () -> Unit,
    modifier: Modifier = Modifier,
    isLastPlayed: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val containerModifier = if (isLastPlayed) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF8B5CF6).copy(alpha = 0.12f))
            .border(1.2.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp)
    }

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with Duration Badge & Bottom Progress Bar
        Box(
            modifier = Modifier
                .width(116.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141624))
                .then(
                    if (isLastPlayed) Modifier.border(1.2.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(12.dp))
                    else Modifier.border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                )
        ) {
            AsyncImage(
                model = video.uri,
                contentDescription = video.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Vibrant Red NEW Badge Overlaid on Video Thumbnail (Exact match to reference screenshot)
            if (video.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PlayMeDesign.BadgeNewRedBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = PlayMeDesign.BadgeNewRedText,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Timeline Progress calculation
            val progress = if (video.progressFraction > 0f) {
                video.progressFraction
            } else if (video.lastPlayedPositionMs > 0L && video.durationMs > 0L) {
                (video.lastPlayedPositionMs.toFloat() / video.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            // Duration Badge on bottom-right (Elevated above progress bar)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = if (progress > 0f) 7.dp else 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.88f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Timeline Progress Bar along bottom edge (Vibrant Green PLAYit Style)
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Color(0xFF27272A).copy(alpha = 0.85f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.5.dp)
                            .background(Color(0xFF10B981))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Metadata Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Title Row + Last Played Tag Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = video.displayName,
                    color = if (isLastPlayed) PlayMeDesign.HighlightLastPlayed else Color(0xFFF4F4F5),
                    fontSize = 13.5.sp,
                    fontWeight = if (isLastPlayed) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isLastPlayed) {
                    Box(
                        modifier = Modifier
                            .background(PlayMeDesign.BadgeIndigoBg, RoundedCornerShape(6.dp))
                            .border(0.8.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = PlayMeDesign.ElectricViolet,
                                modifier = Modifier.size(9.dp)
                            )
                            Text(
                                text = "LAST PLAYED",
                                color = PlayMeDesign.BadgeIndigoText,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle Line 1: Folder · Xm left
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = video.folderName,
                    color = Color(0xFF71717A),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (video.lastPlayedPositionMs > 1000L) {
                    val mins = TimeUnit.MILLISECONDS.toMinutes(video.lastPlayedPositionMs)
                    val secs = TimeUnit.MILLISECONDS.toSeconds(video.lastPlayedPositionMs) % 60
                    val hours = TimeUnit.MILLISECONDS.toHours(video.lastPlayedPositionMs)
                    val playedStr = if (hours > 0) String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins % 60, secs)
                    else String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                    Text(
                        text = " · ",
                        color = Color(0xFF52525B),
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = "▶ $playedStr",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (video.remainingText != null) {
                    Text(
                        text = " · ",
                        color = Color(0xFF52525B),
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = video.remainingText!!,
                        color = PlayMeDesign.BadgeIndigoText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle Line 2: 485 MB · 1080p · MP4
            Text(
                text = "${video.formattedSize}  ·  ${video.simpleResolution}  ·  ${video.extension}",
                color = Color(0xFF71717A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // More Options Button
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF71717A),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .background(PlayMeDesign.DarkGlassElevated)
                    .border(1.dp, PlayMeDesign.DarkGlassBorder, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Play Video", color = Color.White, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        menuExpanded = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hide to Private Vault", color = Color.White, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        menuExpanded = false
                        onMoveToVault()
                    }
                )
            }
        }
    }
}

/**
 * Grid Mode Video Card: 2-column PlayIt style with 16:10 thumbnail and bottom title/details.
 */
@Composable
fun VideoCardGrid(
    video: VideoItem,
    onClick: () -> Unit,
    onMoveToVault: () -> Unit,
    modifier: Modifier = Modifier,
    isLastPlayed: Boolean = false
) {
    val containerModifier = if (isLastPlayed) {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF8B5CF6).copy(alpha = 0.12f))
            .border(1.2.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(5.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    }

    Column(
        modifier = containerModifier
    ) {
        // Thumbnail with 16:10 aspect ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141624))
                .then(
                    if (isLastPlayed) Modifier.border(1.2.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(12.dp))
                    else Modifier.border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                )
        ) {
            AsyncImage(
                model = video.uri,
                contentDescription = video.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top-left: Vibrant Red NEW Badge Overlaid on Video Thumbnail
            if (video.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PlayMeDesign.BadgeNewRedBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = PlayMeDesign.BadgeNewRedText,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            } else if (isLastPlayed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(PlayMeDesign.BadgeIndigoBg, RoundedCornerShape(4.dp))
                        .border(0.8.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = "LAST PLAYED",
                        color = PlayMeDesign.BadgeIndigoText,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Timeline Progress calculation
            val progress = if (video.progressFraction > 0f) {
                video.progressFraction
            } else if (video.lastPlayedPositionMs > 0L && video.durationMs > 0L) {
                (video.lastPlayedPositionMs.toFloat() / video.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            // Duration Pill on bottom-right (Elevated above progress bar)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = if (progress > 0f) 7.dp else 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.88f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Glowing Timeline Progress Bar with dark track along bottom edge
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFF3F3F46).copy(alpha = 0.85f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .background(Color(0xFF8B5CF6))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title with Cyan highlight when last played
        Text(
            text = video.displayName,
            color = if (isLastPlayed) PlayMeDesign.HighlightLastPlayed else Color(0xFFF4F4F5),
            fontSize = 11.5.sp,
            fontWeight = if (isLastPlayed) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Grid Specs Row: 485 MB • 1080p (Clean Dot Separated)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = video.formattedSize,
                color = Color(0xFFA1A1AA),
                fontSize = 10.sp
            )

            Text(
                text = "•",
                color = Color(0xFF71788D),
                fontSize = 10.sp
            )

            val is4k = video.resolutionBadge.contains("4K", ignoreCase = true)
            val is720 = video.resolutionBadge.contains("720", ignoreCase = true)
            val textCol = when {
                is4k -> Color(0xFFC084FC)
                is720 -> Color(0xFF22D3EE)
                else -> Color(0xFFCBD5E1)
            }

            Text(
                text = video.resolutionBadge.ifBlank { "HD" },
                color = textCol,
                fontSize = 10.sp,
                fontWeight = if (is4k) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Category Folder Card with themed colors, count badge, and 2-column grid support.
 */
@Composable
fun FolderCard(
    folder: VideoFolder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGridMode: Boolean = false
) {
    if (isGridMode) {
        FolderCardGrid(folder = folder, onClick = onClick, modifier = modifier)
    } else {
        FolderCardList(folder = folder, onClick = onClick, modifier = modifier)
    }
}

@Composable
fun FolderCardList(
    folder: VideoFolder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lowerName = folder.name.lowercase(Locale.getDefault())
    val (icon, bgCol, iconCol) = when {
        lowerName.contains("camera") || lowerName.contains("dcim") ->
            Triple(Icons.Default.CameraAlt, PlayMeDesign.FolderCameraBg, PlayMeDesign.FolderCameraText)
        lowerName.contains("download") ->
            Triple(Icons.Default.Download, PlayMeDesign.FolderDownloadsBg, PlayMeDesign.FolderDownloadsText)
        lowerName.contains("movie") || lowerName.contains("film") ->
            Triple(Icons.Default.Movie, PlayMeDesign.FolderMoviesBg, PlayMeDesign.FolderMoviesText)
        lowerName.contains("record") || lowerName.contains("screen") ->
            Triple(Icons.Default.Videocam, PlayMeDesign.FolderRecordsBg, PlayMeDesign.FolderRecordsText)
        else ->
            Triple(Icons.Default.Folder, PlayMeDesign.FolderCameraBg, PlayMeDesign.FolderCameraText)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .darkGlassCard(shape = RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box with tinted gradient background
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgCol)
                .border(1.dp, bgCol.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconCol,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Folder Title and Video Count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${folder.videoCount} videos",
                color = Color(0xFFA1A1AA),
                fontSize = 11.5.sp
            )
        }

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF71717A),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 2-Column Grid Folder Card
 */
@Composable
fun FolderCardGrid(
    folder: VideoFolder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lowerName = folder.name.lowercase(Locale.getDefault())
    val (icon, bgCol, iconCol) = when {
        lowerName.contains("camera") || lowerName.contains("dcim") ->
            Triple(Icons.Default.CameraAlt, PlayMeDesign.FolderCameraBg, PlayMeDesign.FolderCameraText)
        lowerName.contains("download") ->
            Triple(Icons.Default.Download, PlayMeDesign.FolderDownloadsBg, PlayMeDesign.FolderDownloadsText)
        lowerName.contains("movie") || lowerName.contains("film") ->
            Triple(Icons.Default.Movie, PlayMeDesign.FolderMoviesBg, PlayMeDesign.FolderMoviesText)
        lowerName.contains("record") || lowerName.contains("screen") ->
            Triple(Icons.Default.Videocam, PlayMeDesign.FolderRecordsBg, PlayMeDesign.FolderRecordsText)
        else ->
            Triple(Icons.Default.Folder, PlayMeDesign.FolderCameraBg, PlayMeDesign.FolderCameraText)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .darkGlassCard(shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgCol)
                .border(1.dp, bgCol.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconCol,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = folder.name,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${folder.videoCount} videos",
            color = Color(0xFFA1A1AA),
            fontSize = 11.sp
        )
    }
}

/**
 * History Carousel Card: Horizontal card with glowing progress bar on bottom and remaining time.
 */
@Composable
fun HistoryCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(132.dp)
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        // Thumbnail with 16:10 aspect ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141624))
        ) {
            AsyncImage(
                model = video.uri,
                contentDescription = video.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Vibrant Emerald Green NEW Badge
            if (video.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PlayMeDesign.BadgeNewGreenBg)
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = PlayMeDesign.BadgeNewGreenText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Duration Pill on bottom-right (Elevated above progress bar)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 7.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.88f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Glowing Timeline Progress Bar with dark track along bottom edge
            val progressFraction = if (video.progressFraction > 0f) {
                video.progressFraction
            } else {
                0.35f
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF3F3F46).copy(alpha = 0.85f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(4.dp)
                        .background(Color(0xFF8B5CF6))
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = video.displayName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle (Shows how long was played / remaining time e.g. "5m left")
        Text(
            text = video.remainingText ?: video.folderName,
            color = if (video.remainingText != null) PlayMeDesign.BadgeIndigoText else Color(0xFFA1A1AA),
            fontSize = 11.sp,
            fontWeight = if (video.remainingText != null) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
