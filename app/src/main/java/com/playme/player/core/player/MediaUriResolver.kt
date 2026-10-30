package com.playme.player.core.player

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.playme.player.core.model.VideoItem
import java.io.File

object MediaUriResolver {

    private const val TAG = "MediaUriResolver"

    fun resolve(context: Context, video: VideoItem): Uri {
        // 1. MediaStore content URIs are the canonical, scoped-storage-safe source.
        //    Do not probe them with openAssetFileDescriptor: some providers return null
        //    even when the URI is perfectly playable.
        if (video.uri != Uri.EMPTY && video.uri.scheme == "content") {
            Log.d(TAG, "Using content URI: ${video.uri}")
            return video.uri
        }

        // 2. Query MediaStore for canonical content URI using file path
        if (video.path.isNotEmpty()) {
            try {
                val projection = arrayOf(MediaStore.Video.Media._ID)
                val selection = "${MediaStore.Video.Media.DATA} = ?"
                val selectionArgs = arrayOf(video.path)

                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                        val id = cursor.getLong(idIndex)
                        val resolved = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                        Log.d(TAG, "Resolved path '${video.path}' to content URI: $resolved")
                        return resolved
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore lookup by path failed: ${e.message}")
            }

            // 3. If file exists on disk, fallback to file:// URI
            val file = File(video.path)
            if (file.exists() && file.canRead()) {
                Log.d(TAG, "Using file URI for path: ${video.path}")
                return Uri.fromFile(file)
            }
        }

        // 4. Return video.uri or empty fallback
        return if (video.uri != Uri.EMPTY) video.uri else Uri.EMPTY
    }
}
