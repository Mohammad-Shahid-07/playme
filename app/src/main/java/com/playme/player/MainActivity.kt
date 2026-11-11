package com.playme.player

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.playme.player.core.designsystem.PlayMeTheme
import com.playme.player.core.permissions.StorageAccess
import com.playme.player.feature.permissions.StorageAccessGate

class MainActivity : ComponentActivity() {

    /**
     * Re-read on every resume and permission result so returning from Settings
     * or accepting the runtime dialog unlocks the library without restarting.
     */
    private var storageAccessGranted by mutableStateOf(false)
    private var storagePermanentlyDenied by mutableStateOf(false)
    private var pendingManualGrantTapAtMs = 0L

    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = StorageAccess.hasLibraryAccess(this)
        storageAccessGranted = granted

        if (!StorageAccess.requiresAllFilesAccess() &&
            Manifest.permission.READ_EXTERNAL_STORAGE in results
        ) {
            handleLegacyStorageResult(granted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        syncStoragePermissionState()

        if (savedInstanceState == null &&
            (StorageAccess.requiresAllFilesAccess() || !storagePermanentlyDenied)
        ) {
            requestMediaPermissions()
        }

        setContent {
            PlayMeTheme {
                if (!storageAccessGranted) {
                    StorageAccessGate(
                        onGrantAccess = ::handleStorageAccessRequest,
                        usesAllFilesAccess = StorageAccess.requiresAllFilesAccess(),
                        isPermanentlyDenied = storagePermanentlyDenied,
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
        syncStoragePermissionState()
    }

    private fun syncStoragePermissionState() {
        val granted = StorageAccess.hasLibraryAccess(this)
        storageAccessGranted = granted
        if (StorageAccess.requiresAllFilesAccess()) {
            storagePermanentlyDenied = false
            return
        }
        if (granted) {
            updateLegacyDenialState(hadRationale = false, permanentlyDenied = false)
        } else {
            storagePermanentlyDenied = prefs().getBoolean(KEY_PERMANENTLY_DENIED, false) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
        }
    }

    private fun handleStorageAccessRequest() {
        if (StorageAccess.requiresAllFilesAccess()) {
            StorageAccess.openSettings(this)
            return
        }
        if (storagePermanentlyDenied) {
            StorageAccess.openAppSettings(this)
            return
        }
        pendingManualGrantTapAtMs = SystemClock.elapsedRealtime()
        mediaPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    private fun handleLegacyStorageResult(granted: Boolean) {
        val fromManualTap = pendingManualGrantTapAtMs > 0L
        val elapsed = SystemClock.elapsedRealtime() - pendingManualGrantTapAtMs
        pendingManualGrantTapAtMs = 0L

        if (granted) {
            updateLegacyDenialState(hadRationale = false, permanentlyDenied = false)
            return
        }

        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (showRationale) {
            updateLegacyDenialState(hadRationale = true, permanentlyDenied = false)
            return
        }

        val instantAutoDeny = fromManualTap && elapsed < AUTO_DENY_THRESHOLD_MS
        val hadRationale = prefs().getBoolean(KEY_HAD_RATIONALE, false)
        val permanentlyDenied = hadRationale || instantAutoDeny
        updateLegacyDenialState(hadRationale = hadRationale, permanentlyDenied = permanentlyDenied)
        if (instantAutoDeny) {
            StorageAccess.openAppSettings(this)
        }
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

    private fun updateLegacyDenialState(hadRationale: Boolean, permanentlyDenied: Boolean) {
        storagePermanentlyDenied = permanentlyDenied
        prefs().edit()
            .putBoolean(KEY_HAD_RATIONALE, hadRationale)
            .putBoolean(KEY_PERMANENTLY_DENIED, permanentlyDenied)
            .apply()
    }

    private fun prefs() = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val PREFS_NAME = "storage_permission_state"
        const val KEY_HAD_RATIONALE = "had_storage_rationale"
        const val KEY_PERMANENTLY_DENIED = "storage_permanently_denied"
        const val AUTO_DENY_THRESHOLD_MS = 350L
    }
}
