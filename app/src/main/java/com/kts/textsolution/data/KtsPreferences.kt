package com.kts.textsolution.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils

object KtsPreferences {
    private const val PREFS_NAME = "kts_prefs"
    private const val KEY_AUTO_READ_BACKGROUND = "pref_auto_read_background"
    private const val KEY_SPEECH_RATE = "pref_speech_rate"
    private const val KEY_TTS_ENGINE = "pref_tts_engine"
    private const val KEY_TTS_LANGUAGE = "pref_tts_language"
    private const val KEY_TTS_VOICE = "pref_tts_voice"
    private const val KEY_TTS_PITCH = "pref_tts_pitch"
    private const val KEY_TTS_VOLUME = "pref_tts_volume"
    private const val KEY_ANNOUNCE_CHUNK_NUMBERS = "pref_announce_chunk_numbers"
    private const val KEY_SPEECH_FILTER_MODE = "pref_speech_filter_mode"
    private const val KEY_CLIPBOARD_HISTORY_ENABLED = "pref_clipboard_history_enabled"
    private const val KEY_HAPTIC_FEEDBACK_ENABLED = "pref_haptic_feedback_enabled"
    private const val KEY_AUTO_CLIPBOARD = "pref_auto_clipboard"
    private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
    private const val KEY_FONT_SIZE = "pref_font_size"
    private const val KEY_DARK_THEME = "pref_dark_theme"
    private const val KEY_LAST_BACKUP_TIME = "pref_last_backup_time"
    private const val KEY_SAVED_SESSIONS_JSON = "pref_saved_sessions_json"
    private const val KEY_LAST_IMPORTED_CLIP = "pref_last_imported_clip"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAutoClipboardEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_CLIPBOARD, true)

    fun setAutoClipboardEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_CLIPBOARD, enabled).apply()
    }

    fun isHighContrastEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HIGH_CONTRAST, false)

    fun setHighContrastEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun getFontSize(context: Context): Float =
        prefs(context).getFloat(KEY_FONT_SIZE, 18f)

    fun setFontSize(context: Context, size: Float) {
        prefs(context).edit().putFloat(KEY_FONT_SIZE, size).apply()
    }

    fun isDarkThemeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_THEME, true)

    fun setDarkThemeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    fun getLastBackupTime(context: Context): Long =
        prefs(context).getLong(KEY_LAST_BACKUP_TIME, 0L)

    fun setLastBackupTime(context: Context, timestamp: Long) {
        prefs(context).edit().putLong(KEY_LAST_BACKUP_TIME, timestamp).apply()
    }

    fun getSavedSessionsJson(context: Context): String =
        prefs(context).getString(KEY_SAVED_SESSIONS_JSON, "") ?: ""

    fun setSavedSessionsJson(context: Context, json: String) {
        prefs(context).edit().putString(KEY_SAVED_SESSIONS_JSON, json).apply()
    }

    fun getLastImportedClip(context: Context): String =
        prefs(context).getString(KEY_LAST_IMPORTED_CLIP, "") ?: ""

    fun setLastImportedClip(context: Context, text: String) {
        prefs(context).edit().putString(KEY_LAST_IMPORTED_CLIP, text).apply()
    }

    fun isHapticFeedbackEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, true)

    fun setHapticFeedbackEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, enabled).apply()
    }

    fun isClipboardHistoryEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CLIPBOARD_HISTORY_ENABLED, true)

    fun setClipboardHistoryEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_CLIPBOARD_HISTORY_ENABLED, enabled).apply()
    }

    fun getSpeechFilterMode(context: Context): SpeechFilterMode {
        val modeName = prefs(context).getString(KEY_SPEECH_FILTER_MODE, SpeechFilterMode.Some.name)
        return try {
            SpeechFilterMode.valueOf(modeName ?: SpeechFilterMode.Some.name)
        } catch (e: Exception) {
            SpeechFilterMode.Some
        }
    }

    fun setSpeechFilterMode(context: Context, mode: SpeechFilterMode) {
        prefs(context).edit().putString(KEY_SPEECH_FILTER_MODE, mode.name).apply()
    }

    fun isAnnounceChunkNumbersEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ANNOUNCE_CHUNK_NUMBERS, true)

    fun setAnnounceChunkNumbersEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ANNOUNCE_CHUNK_NUMBERS, enabled).apply()
    }

    fun isAutoReadBackgroundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_READ_BACKGROUND, false)

    fun setAutoReadBackgroundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_READ_BACKGROUND, enabled).apply()
    }

    fun getSpeechRate(context: Context): Float =
        prefs(context).getFloat(KEY_SPEECH_RATE, 1.0f)

    fun setSpeechRate(context: Context, rate: Float) {
        prefs(context).edit().putFloat(KEY_SPEECH_RATE, rate).apply()
    }

    fun getTtsEngine(context: Context): String =
        prefs(context).getString(KEY_TTS_ENGINE, "") ?: ""

    fun setTtsEngine(context: Context, engine: String) {
        prefs(context).edit().putString(KEY_TTS_ENGINE, engine).apply()
    }

    fun getTtsLanguage(context: Context): String =
        prefs(context).getString(KEY_TTS_LANGUAGE, "auto") ?: "auto"

    fun setTtsLanguage(context: Context, lang: String) {
        prefs(context).edit().putString(KEY_TTS_LANGUAGE, lang).apply()
    }

    fun getTtsVoice(context: Context): String =
        prefs(context).getString(KEY_TTS_VOICE, "") ?: ""

    fun setTtsVoice(context: Context, voice: String) {
        prefs(context).edit().putString(KEY_TTS_VOICE, voice).apply()
    }

    fun getTtsPitch(context: Context): Float =
        prefs(context).getFloat(KEY_TTS_PITCH, 1.0f)

    fun setTtsPitch(context: Context, pitch: Float) {
        prefs(context).edit().putFloat(KEY_TTS_PITCH, pitch).apply()
    }

    fun getTtsVolume(context: Context): Float =
        prefs(context).getFloat(KEY_TTS_VOLUME, 1.0f)

    fun setTtsVolume(context: Context, volume: Float) {
        prefs(context).edit().putFloat(KEY_TTS_VOLUME, volume).apply()
    }

    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expected = ComponentName(context, serviceClass).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val componentName = splitter.next()
            if (componentName.equals(expected, ignoreCase = true) ||
                componentName.contains(serviceClass.simpleName, ignoreCase = true)
            ) {
                return true
            }
        }
        return false
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                }
            }
        }
    }
}

