package com.playme.player

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.video.VideoFrameDecoder
import java.io.File

class PlayMeApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("PlayMeCrash", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
            try {
                val crashFile = File(filesDir, "crash_dump.txt")
                crashFile.writeText(
                    "Thread: ${thread.name}\nException: ${throwable::class.java.name}: ${throwable.message}\n" +
                    Log.getStackTraceString(throwable)
                )
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("video_thumbnails_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .build()
    }
}
