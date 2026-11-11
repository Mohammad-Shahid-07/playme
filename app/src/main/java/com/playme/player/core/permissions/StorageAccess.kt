package com.playme.player.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Gates library scanning on storage permission across Android versions:
 * - API 30+ requires `MANAGE_EXTERNAL_STORAGE` (Settings toggle) so deep scans
 *   can see videos in `.nomedia` folders (Telegram/WhatsApp).
 * - API 26–29 uses the runtime `READ_EXTERNAL_STORAGE` permission (paired with
 *   `requestLegacyExternalStorage` on Android 10).
 */
object StorageAccess {

    fun requiresAllFilesAccess(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun isRequired(): Boolean = true

    fun hasLibraryAccess(context: Context): Boolean = if (requiresAllFilesAccess()) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasDeepScanAccess(context: Context): Boolean = if (requiresAllFilesAccess()) {
        Environment.isExternalStorageManager()
    } else {
        hasLibraryAccess(context) &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Environment.isExternalStorageLegacy())
    }

    /** Opens All Files Access settings on API 30+, or App Info settings on API < 30. */
    fun openSettings(context: Context) {
        if (requiresAllFilesAccess()) {
            val appIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                .setData(Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            runCatching { context.startActivity(appIntent) }
                .recoverCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                .recoverCatching { openAppSettings(context) }
            return
        }
        openAppSettings(context)
    }

    /** Opens App Info so the user can toggle Storage after permanent denial. */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(intent) }
            .recoverCatching {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
    }
}
