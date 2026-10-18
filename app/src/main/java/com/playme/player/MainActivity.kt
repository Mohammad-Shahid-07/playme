package com.playme.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.core.designsystem.PlayMeTheme
import com.playme.player.feature.player.ui.LandscapePlayerScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PlayMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PlayMeColors.ObsidianDark
                ) {
                    LandscapePlayerScreen(
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}
