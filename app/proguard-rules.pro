# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Tofik\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Media3 ExoPlayer keep rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase

# FFmpegKit — JNI entry points and session callbacks are reached by name.
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.smartexception.** { *; }
-dontwarn com.arthenica.**

# LibVLC — the JNI layer resolves these classes by name at JNI_OnLoad.
# Without this, release builds fail with:
#   FindClass(org/videolan/libvlc/interfaces/IMedia$Track) failed
#   UnsatisfiedLinkError: JNI_ERR returned from JNI_OnLoad in libvlcjni.so
-keep class org.videolan.libvlc.** { *; }
-keep class org.videolan.libvlc.interfaces.** { *; }
-keep class org.videolan.medialibrary.** { *; }
-dontwarn org.videolan.**
