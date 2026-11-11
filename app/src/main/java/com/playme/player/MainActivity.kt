package com.playme.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.playme.player.core.designsystem.PlayMeTheme
import com.playme.player.core.permissions.StorageAccess
import com.playme.player.feature.permissions.StorageAccessGate

class MainActivity : ComponentActivity() {

    /**
     * Re-read on every resume so returning from the system Settings toggle
     * unlocks the library immediately, without an app restart.
     */
    private var storageAccessGranted by mutableStateOf(false)

    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // The library scan reacts to whatever access was actually granted.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        storageAccessGranted = StorageAccess.hasAllFilesAccess()
        requestMediaPermissions()

        setContent {
            PlayMeTheme {
                if (StorageAccess.isRequired() && !storageAccessGranted) {
                    StorageAccessGate(
                        onGrantAccess = { StorageAccess.openSettings(this@MainActivity) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PlayMeApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        storageAccessGranted = StorageAccess.hasAllFilesAccess()
    }

    private fun requestMediaPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isGranted(Manifest.permission.READ_MEDIA_VIDEO)) {
                permissions += Manifest.permission.READ_MEDIA_VIDEO
            }
            if (!isGranted(Manifest.permission.READ_MEDIA_AUDIO)) {
                permissions += Manifest.permission.READ_MEDIA_AUDIO
            }
        } else if (!isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (permissions.isNotEmpty()) {
            mediaPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
