package com.playme.player.core.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings

/**
 * All Files Access is a hard requirement: without it the library can only see
 * what MediaStore indexed, so videos saved by Telegram/WhatsApp (which write
 * `.nomedia` markers) stay invisible.
 */
object StorageAccess {

    fun isRequired(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun hasAllFilesAccess(): Boolean = if (isRequired()) {
        Environment.isExternalStorageManager()
    } else {
        true
    }

    /** Opens the system toggle; falls back to the app-level list on some ROMs. */
    fun openSettings(context: Context) {
        if (!isRequired()) return

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
    }
}
