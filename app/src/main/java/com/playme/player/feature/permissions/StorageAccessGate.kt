package com.playme.player.feature.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeColors

/**
 * Blocking first-run step for All Files Access.
 *
 * Android cannot grant this with a runtime dialog, so the only correct flow is
 * a single, explicit screen that sends the user to the system toggle. There is
 * deliberately no dismiss action: without the grant the library cannot see
 * videos stored in `.nomedia` folders.
 */
@Composable
fun StorageAccessGate(
    onGrantAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayMeColors.Workbench)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x2E757BF8), Color(0x00080910))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PLAYME",
                color = PlayMeColors.LavenderText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            IconTile()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Storage access",
                color = PlayMeColors.TextPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.4).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PlayMe reads videos straight from your device. " +
                    "Grant file access once so folders from Telegram, WhatsApp, " +
                    "and every other app appear in your library.",
                color = PlayMeColors.TextSecondary,
                fontSize = 13.5.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 310.dp)
            )

            Spacer(modifier = Modifier.height(34.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = PlayMeColors.LavenderGlow,
                        spotColor = PlayMeColors.LavenderGlow
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(PlayMeColors.VioletGradient)
                    .clickable(onClick = onGrantAccess),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Grant access",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Android opens Settings. Turn on “Allow access to manage all files”, then return here.",
                color = PlayMeColors.TextTertiary,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}

@Composable
private fun IconTile() {
    Box(
        modifier = Modifier
            .size(78.dp)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = PlayMeColors.LavenderGlow,
                spotColor = PlayMeColors.LavenderGlow
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x2E6366F1), Color(0x148B5CF6))
                )
            )
            .border(1.dp, Color(0x4D757BF8), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.FolderOpen,
            contentDescription = null,
            tint = Color(0xFFA5B4FC),
            modifier = Modifier.size(34.dp)
        )
    }
}

