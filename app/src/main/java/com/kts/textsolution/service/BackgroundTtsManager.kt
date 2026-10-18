package com.kts.textsolution.service

import android.content.Context
import com.kts.textsolution.data.KtsPreferences
import com.kts.textsolution.data.SpeechFilterMode
import com.kts.textsolution.data.TextSpeechFilter
import com.kts.textsolution.data.TtsEngineInfo
import com.kts.textsolution.data.TtsManager
import com.kts.textsolution.data.TtsVoiceInfo
import java.util.Locale

object BackgroundTtsManager {
    private var ttsManager: TtsManager? = null
    private var appContext: Context? = null

    var isSpeaking: Boolean = false
        private set

    var isPaused: Boolean = false
        private set

    var speechRate: Float = 1.0f
    var pitch: Float = 1.0f
    var volume: Float = 1.0f
    var selectedEngine: String = ""
    var selectedLanguage: String = "auto"
    var selectedVoice: String = ""
    var filterMode: SpeechFilterMode = SpeechFilterMode.Some

    private var currentSpeakingText: String = ""
    private var pausedRemainingText: String = ""
    private var lastSpokenText: String = ""
    private var lastSpokenTimestamp: Long = 0L

    var onStateChange: ((Boolean) -> Unit)? = null
    var onNotificationUpdate: ((isReading: Boolean, textSnippet: String) -> Unit)? = null

    fun init(context: Context) {
        if (appContext == null) {
            val app = context.applicationContext
            appContext = app
            speechRate = KtsPreferences.getSpeechRate(app)
            pitch = KtsPreferences.getTtsPitch(app)
            volume = KtsPreferences.getTtsVolume(app)
            selectedEngine = KtsPreferences.getTtsEngine(app)
            selectedLanguage = KtsPreferences.getTtsLanguage(app)
            selectedVoice = KtsPreferences.getTtsVoice(app)
            filterMode = KtsPreferences.getSpeechFilterMode(app)

            KtsMediaSessionManager.init(app)
            KtsMediaSessionManager.onPauseRequested = {
                pause()
            }
            KtsMediaSessionManager.onStopRequested = {
                stop()
            }
            KtsMediaSessionManager.onPlayRequested = {
                resumeOrPlay()
            }

            ttsManager = TtsManager(
                context = app,
                onStateChange = { speaking ->
                    isSpeaking = speaking
                    if (speaking) {
                        val snippet = if (currentSpeakingText.length > 50) currentSpeakingText.take(47) + "..." else currentSpeakingText
                        KtsMediaSessionManager.setPlaying(snippet = snippet)
                        onNotificationUpdate?.invoke(true, snippet)
                    } else if (!isPaused) {
                        KtsMediaSessionManager.setStopped()
                        onNotificationUpdate?.invoke(false, "")
                    }
                    onStateChange?.invoke(speaking)
                },
                onError = {
                    isSpeaking = false
                    isPaused = false
                    pausedRemainingText = ""
                    KtsMediaSessionManager.setStopped()
                    onStateChange?.invoke(false)
                    onNotificationUpdate?.invoke(false, "")
                },
                initialEngine = selectedEngine.ifBlank { null },
            )
        }
    }

    fun isBackgroundReadingEnabled(context: Context): Boolean =
        KtsPreferences.isAutoReadBackgroundEnabled(context)

    fun switchEngine(context: Context, enginePackage: String, onComplete: ((Boolean) -> Unit)? = null) {
        selectedEngine = enginePackage
        KtsPreferences.setTtsEngine(context, enginePackage)
        // Reset voice selection when engine changes
        selectedVoice = ""
        KtsPreferences.setTtsVoice(context, "")
        ttsManager?.switchEngine(enginePackage.ifBlank { null }, onComplete)
    }

    fun updateSpeechRate(context: Context, rate: Float) {
        speechRate = rate
        KtsPreferences.setSpeechRate(context, rate)
    }

    fun updatePitch(context: Context, newPitch: Float) {
        pitch = newPitch
        KtsPreferences.setTtsPitch(context, newPitch)
    }

    fun updateVolume(context: Context, newVolume: Float) {
        volume = newVolume
        KtsPreferences.setTtsVolume(context, newVolume)
    }

    fun updateLanguage(context: Context, lang: String) {
        selectedLanguage = lang
        KtsPreferences.setTtsLanguage(context, lang)
        selectedVoice = ""
        KtsPreferences.setTtsVoice(context, "")
    }

    fun updateVoice(context: Context, voiceName: String) {
        selectedVoice = voiceName
        KtsPreferences.setTtsVoice(context, voiceName)
    }

    fun updateFilterMode(context: Context, mode: SpeechFilterMode) {
        filterMode = mode
        KtsPreferences.setSpeechFilterMode(context, mode)
    }

    fun getInstalledEngines(): List<TtsEngineInfo> =
        ttsManager?.getInstalledEngines() ?: emptyList()

    fun getAvailableVoices(forLocale: Locale? = null): List<TtsVoiceInfo> =
        ttsManager?.getAvailableVoices(forLocale) ?: emptyList()

    fun getAvailableLanguages(): List<Locale> =
        ttsManager?.getAvailableLanguages() ?: emptyList()

    @Synchronized
    fun speak(text: String, source: String = "") {
        var clean = text.trim()
        val readMoreSuffixes = listOf(
            "... Read more", "...Read more", "… Read more", "…Read more", "Read more", "Read More",
            "… കൂടുതൽ വായിക്കുക", "... കൂടുതൽ വായിക്കുക", "കൂടുതൽ വായിക്കുക",
            "… See more", "... See more", "See more",
            "… Leer más", "... Leer más", "Leer más",
            "… और पढ़ें", "... और पढ़ें", "और पढ़ें"
        )
        for (suffix in readMoreSuffixes) {
            if (clean.endsWith(suffix, ignoreCase = true)) {
                clean = clean.substring(0, clean.length - suffix.length).trimEnd()
            }
        }

        if (source != "TestVoice") {
            clean = try {
                TextSpeechFilter.filter(clean, filterMode)
            } catch (e: Exception) {
                clean
            }
        }

        if (clean.isBlank()) return

        val now = System.currentTimeMillis()
        val isFullTextSupersedingTruncated = lastSpokenText.isNotBlank() &&
                clean.length > lastSpokenText.length &&
                clean.startsWith(lastSpokenText.take(minOf(30, lastSpokenText.length)))

        // Prevent duplicate speech if triggered repeatedly within 3.5 seconds, UNLESS this is a fuller version
        if (!isFullTextSupersedingTruncated && clean == lastSpokenText && (now - lastSpokenTimestamp < 3500)) {
            return
        }
        lastSpokenText = clean
        lastSpokenTimestamp = now

        currentSpeakingText = clean
        pausedRemainingText = ""
        isPaused = false
        isSpeaking = true

        val snippet = if (clean.length > 50) clean.take(47) + "..." else clean
        KtsMediaSessionManager.setPlaying(snippet = snippet)
        onNotificationUpdate?.invoke(true, snippet)

        ttsManager?.speak(
            text = clean,
            rate = speechRate,
            pitch = pitch,
            volume = volume,
            voiceName = selectedVoice,
            languageCode = selectedLanguage,
        )
    }

    fun pause() {
        if (!isSpeaking) return
        val offset = ttsManager?.lastSpokenOffset ?: 0
        pausedRemainingText = if (offset > 0 && offset < currentSpeakingText.length) {
            currentSpeakingText.substring(offset).trim()
        } else {
            currentSpeakingText
        }
        ttsManager?.stop()
        isSpeaking = false
        isPaused = true
        KtsMediaSessionManager.setPaused()
        onStateChange?.invoke(false)
        onNotificationUpdate?.invoke(false, "")
    }

    fun resumeOrPlay() {
        if (isPaused && pausedRemainingText.isNotBlank()) {
            val textToResume = pausedRemainingText
            pausedRemainingText = ""
            isPaused = false
            speak(textToResume, "ResumePlayback")
        } else if (lastSpokenText.isNotBlank()) {
            speak(lastSpokenText, "ReplayPlayback")
        }
    }

    fun stop() {
        ttsManager?.stop()
        isSpeaking = false
        isPaused = false
        pausedRemainingText = ""
        KtsMediaSessionManager.setStopped()
        onStateChange?.invoke(false)
        onNotificationUpdate?.invoke(false, "")
    }

    fun shutdown() {
        ttsManager?.shutdown()
        ttsManager = null
        appContext = null
        isSpeaking = false
        isPaused = false
        pausedRemainingText = ""
        KtsMediaSessionManager.release()
    }
}

