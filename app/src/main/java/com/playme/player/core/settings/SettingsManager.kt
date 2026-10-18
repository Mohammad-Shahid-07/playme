package com.playme.player.core.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _globalPlaybackSpeed = MutableStateFlow(prefs.getFloat(KEY_GLOBAL_SPEED, 1.0f))
    val globalPlaybackSpeed: StateFlow<Float> = _globalPlaybackSpeed.asStateFlow()

    private val _preferredAudioLanguage = MutableStateFlow(prefs.getString(KEY_AUDIO_LANG, "default") ?: "default")
    val preferredAudioLanguage: StateFlow<String> = _preferredAudioLanguage.asStateFlow()

    private val _preferredSubtitleLanguage = MutableStateFlow(prefs.getString(KEY_SUBTITLE_LANG, "off") ?: "off")
    val preferredSubtitleLanguage: StateFlow<String> = _preferredSubtitleLanguage.asStateFlow()

    private val _autoGroupSimilarVideos = MutableStateFlow(prefs.getBoolean(KEY_AUTO_GROUP, false))
    val autoGroupSimilarVideos: StateFlow<Boolean> = _autoGroupSimilarVideos.asStateFlow()

    fun setGlobalPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_GLOBAL_SPEED, speed).apply()
        _globalPlaybackSpeed.value = speed
    }

    fun getGlobalPlaybackSpeed(): Float = _globalPlaybackSpeed.value

    fun setFolderPlaybackSpeed(folderPath: String, speed: Float?) {
        val jsonStr = prefs.getString(KEY_FOLDER_SPEEDS, "{}") ?: "{}"
        val json = try { JSONObject(jsonStr) } catch (e: Exception) { JSONObject() }
        if (speed == null || speed == 1.0f) {
            json.remove(folderPath)
        } else {
            json.put(folderPath, speed.toDouble())
        }
        prefs.edit().putString(KEY_FOLDER_SPEEDS, json.toString()).apply()
    }

    fun getFolderPlaybackSpeed(folderPath: String): Float? {
        val jsonStr = prefs.getString(KEY_FOLDER_SPEEDS, "{}") ?: "{}"
        val json = try { JSONObject(jsonStr) } catch (e: Exception) { JSONObject() }
        return if (json.has(folderPath)) json.optDouble(folderPath).toFloat() else null
    }

    fun getAllFolderPlaybackSpeeds(): Map<String, Float> {
        val jsonStr = prefs.getString(KEY_FOLDER_SPEEDS, "{}") ?: "{}"
        val json = try { JSONObject(jsonStr) } catch (e: Exception) { JSONObject() }
        val map = mutableMapOf<String, Float>()
        json.keys().forEach { key ->
            map[key] = json.optDouble(key).toFloat()
        }
        return map
    }

    fun getEffectiveSpeed(folderPath: String?): Float {
        if (!folderPath.isNullOrBlank()) {
            val folderSpeed = getFolderPlaybackSpeed(folderPath)
            if (folderSpeed != null && folderSpeed > 0f) {
                return folderSpeed
            }
        }
        return getGlobalPlaybackSpeed()
    }

    fun setPreferredAudioLanguage(langCode: String) {
        prefs.edit().putString(KEY_AUDIO_LANG, langCode).apply()
        _preferredAudioLanguage.value = langCode
    }

    fun getPreferredAudioLanguage(): String = _preferredAudioLanguage.value

    fun setPreferredSubtitleLanguage(langCode: String) {
        prefs.edit().putString(KEY_SUBTITLE_LANG, langCode).apply()
        _preferredSubtitleLanguage.value = langCode
    }

    fun getPreferredSubtitleLanguage(): String = _preferredSubtitleLanguage.value

    fun setAutoGroupSimilarVideos(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_GROUP, enabled).apply()
        _autoGroupSimilarVideos.value = enabled
    }

    fun isAutoGroupSimilarVideos(): Boolean = _autoGroupSimilarVideos.value

    companion object {
        private const val PREFS_NAME = "playme_settings_prefs"
        private const val KEY_GLOBAL_SPEED = "key_global_playback_speed"
        private const val KEY_FOLDER_SPEEDS = "key_folder_playback_speeds"
        private const val KEY_AUDIO_LANG = "key_preferred_audio_language"
        private const val KEY_SUBTITLE_LANG = "key_preferred_subtitle_language"
        private const val KEY_AUTO_GROUP = "key_auto_group_similar_videos"

        val AVAILABLE_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        val AUDIO_LANGUAGES = listOf(
            "default" to "Default (Media Track)",
            "en" to "English",
            "hi" to "Hindi",
            "es" to "Spanish",
            "ja" to "Japanese",
            "ko" to "Korean",
            "fr" to "French"
        )
        val SUBTITLE_LANGUAGES = listOf(
            "off" to "Off (Disabled)",
            "en" to "English",
            "hi" to "Hindi",
            "es" to "Spanish",
            "ja" to "Japanese",
            "ko" to "Korean",
            "auto" to "Auto Detect"
        )
    }
}
