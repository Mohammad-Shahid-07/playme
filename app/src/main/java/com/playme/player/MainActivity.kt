package com.playme.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.core.designsystem.PlayMeTheme
import com.playme.player.feature.library.ui.LibraryScreen
import com.playme.player.feature.navigation.BottomNavBar
import com.playme.player.feature.navigation.MainNavTab

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Scans auto-trigger in LibraryViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        checkAndRequestStoragePermissions()

        setContent {
            PlayMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PlayMeColors.ObsidianDark
                ) {
                    var currentTab by remember { mutableStateOf(MainNavTab.VIDEO) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentTab) {
                            MainNavTab.VIDEO -> {
                                LibraryScreen(
                                    onVideoClick = { /* Will open player in next phase */ }
                                )
                            }
                            else -> {
                                // Music, Browse, Settings placeholders
                                LibraryScreen(
                                    onVideoClick = { }
                                )
                            }
                        }

                        // Floating Glass Bottom Navigation Dock
                        BottomNavBar(
                            activeTab = currentTab,
                            onTabSelected = { currentTab = it },
                            onBigPlayClick = { /* Quick play latest video */ },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
