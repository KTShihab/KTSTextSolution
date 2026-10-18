package com.kts.textsolution.data

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale
import java.util.UUID

data class TtsEngineInfo(
    val packageName: String,
    val label: String,
)

data class TtsVoiceInfo(
    val name: String,
    val displayName: String,
    val locale: Locale,
)

class TtsManager(
    private val context: Context,
    private val onStateChange: (Boolean) -> Unit,
    private val onError: (String) -> Unit = {},
    initialEngine: String? = null,
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isReady: Boolean = false
        private set

    var currentEngine: String? = initialEngine?.ifBlank { null }
        private set

    var lastSpokenOffset: Int = 0
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingInitCallback: ((Boolean) -> Unit)? = null

    init {
        initTts(currentEngine)
    }

    private fun initTts(engine: String?) {
        isReady = false
        tts = if (engine.isNullOrBlank()) {
            TextToSpeech(context.applicationContext, this)
        } else {
            TextToSpeech(context.applicationContext, this, engine)
        }
    }

    fun switchEngine(enginePackage: String?, onComplete: ((Boolean) -> Unit)? = null) {
        val target = enginePackage?.ifBlank { null }
        if (target == currentEngine && isReady) {
            onComplete?.invoke(true)
            return
        }
        stop()
        shutdown()
        currentEngine = target
        pendingInitCallback = onComplete
        initTts(target)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
            } catch (t: Throwable) {}
            setupListener()
            mainHandler.post {
                pendingInitCallback?.invoke(true)
                pendingInitCallback = null
            }
        } else {
            isReady = false
            mainHandler.post {
                onError("TTS initialization failed for engine: ${currentEngine ?: "default"}")
                pendingInitCallback?.invoke(false)
                pendingInitCallback = null
            }
        }
    }

    private var finalUtteranceId: String? = null

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post { onStateChange(true) }
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == null || utteranceId == finalUtteranceId) {
                    mainHandler.post { onStateChange(false) }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post { onStateChange(false) }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post { onStateChange(false) }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                lastSpokenOffset = start
            }
        })
    }

    fun getInstalledEngines(): List<TtsEngineInfo> {
        val engines = tts?.engines ?: emptyList()
        return engines.map {
            TtsEngineInfo(
                packageName = it.name,
                label = it.label.ifBlank { it.name },
            )
        }.sortedBy { it.label }
    }

    fun getAvailableVoices(forLocale: Locale? = null): List<TtsVoiceInfo> {
        if (!isReady || tts == null) return emptyList()
        val allVoices = try {
            tts?.voices ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }

        val filtered = if (forLocale != null && forLocale.language.isNotBlank()) {
            allVoices.filter { it.locale.language.equals(forLocale.language, ignoreCase = true) }
        } else {
            allVoices
        }

        return filtered.map { voice ->
            val display = buildVoiceLabel(voice)
            TtsVoiceInfo(
                name = voice.name,
                displayName = display,
                locale = voice.locale,
            )
        }.sortedBy { it.displayName }
    }

    private fun buildVoiceLabel(voice: Voice): String {
        val locName = voice.locale.displayName.ifBlank { voice.locale.toString() }
        val quality = if (voice.quality >= Voice.QUALITY_VERY_HIGH) " (HQ)" else ""
        return "${voice.name} - $locName$quality"
    }

    fun getAvailableLanguages(): List<Locale> {
        if (!isReady || tts == null) return listOf(Locale.ENGLISH)
        val langs = try {
            val fromVoices = tts?.voices?.map { it.locale }?.distinctBy { it.language } ?: emptyList()
            if (fromVoices.isNotEmpty()) fromVoices else {
                tts?.availableLanguages?.toList() ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
        return if (langs.isEmpty()) listOf(Locale.ENGLISH) else langs
    }

    fun speak(
        text: String,
        rate: Float = 1.0f,
        pitch: Float = 1.0f,
        volume: Float = 1.0f,
        voiceName: String = "",
        languageCode: String = "auto",
    ) {
        if (!isReady || tts == null) {
            mainHandler.post {
                onError("TTS not ready")
            }
            return
        }

        if (text.isBlank()) return

        try {
            tts?.stop()
        } catch (e: Exception) {}
        lastSpokenOffset = 0

        // Configure speed rate & pitch
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.5f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

        // Determine target locale
        val targetLocale = if (languageCode.isNotBlank() && !languageCode.equals("auto", ignoreCase = true)) {
            resolveLocale(languageCode)
        } else {
            LanguageDetector.detectLocale(text)
        }

        // Configure voice if specified and compatible with target language
        var voiceApplied = false
        if (voiceName.isNotBlank()) {
            try {
                val voice = tts?.voices?.firstOrNull { it.name == voiceName }
                if (voice != null && (voice.locale.language.equals(targetLocale.language, ignoreCase = true) || !languageCode.equals("auto", ignoreCase = true))) {
                    tts?.voice = voice
                    voiceApplied = true
                }
            } catch (e: Exception) {}
        }

        // If specific voice was not applied, configure voice or language dynamically
        if (!voiceApplied) {
            val matchingVoices = try {
                tts?.voices?.filter { it.locale.language.equals(targetLocale.language, ignoreCase = true) }
            } catch (e: Exception) {
                null
            }

            if (!matchingVoices.isNullOrEmpty()) {
                val bestVoice = matchingVoices.firstOrNull {
                    it.locale.country.equals(targetLocale.country, ignoreCase = true) && !it.isNetworkConnectionRequired
                } ?: matchingVoices.firstOrNull {
                    it.locale.country.equals(targetLocale.country, ignoreCase = true)
                } ?: matchingVoices.firstOrNull {
                    !it.isNetworkConnectionRequired
                } ?: matchingVoices.first()

                try {
                    tts?.voice = bestVoice
                    voiceApplied = true
                } catch (e: Exception) {}
            }

            if (!voiceApplied) {
                applyLanguage(targetLocale)
            }
        }

        // Volume parameter via Bundle
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0.0f, 1.0f))
        }

        val maxLength = TextToSpeech.getMaxSpeechInputLength().coerceAtMost(3500)
        val utteranceId = UUID.randomUUID().toString()
        finalUtteranceId = utteranceId

        if (text.length <= maxLength) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } else {
            val parts = splitIntoSpeechChunks(text, maxLength)
            parts.forEachIndexed { index, part ->
                val id = if (index == parts.lastIndex) utteranceId else UUID.randomUUID().toString()
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(part, queueMode, params, id)
            }
        }
    }

    private fun splitIntoSpeechChunks(text: String, maxLength: Int): List<String> {
        if (text.length <= maxLength) return listOf(text)
        val chunks = mutableListOf<String>()
        var remaining = text
        while (remaining.length > maxLength) {
            var splitIndex = remaining.lastIndexOfAny(charArrayOf('.', '\n', '!', '?', ';'), maxLength)
            if (splitIndex < maxLength / 2) {
                splitIndex = remaining.lastIndexOf(' ', maxLength)
            }
            if (splitIndex <= 0) {
                splitIndex = maxLength
            } else {
                splitIndex += 1
            }
            chunks.add(remaining.substring(0, splitIndex).trim())
            remaining = remaining.substring(splitIndex).trim()
        }
        if (remaining.isNotBlank()) {
            chunks.add(remaining)
        }
        return chunks
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {}
        mainHandler.post { onStateChange(false) }
    }

    private fun applyLanguage(targetLocale: Locale) {
        try {
            val avail = tts?.isLanguageAvailable(targetLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (avail >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = targetLocale
            } else {
                val langOnly = Locale(targetLocale.language)
                val langAvail = tts?.isLanguageAvailable(langOnly) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (langAvail >= TextToSpeech.LANG_AVAILABLE) {
                    tts?.language = langOnly
                } else {
                    val res = tts?.setLanguage(targetLocale)
                    if (res == null || res < TextToSpeech.LANG_AVAILABLE) {
                        tts?.setLanguage(langOnly)
                    }
                }
            }
        } catch (e: Exception) {
            try {
                tts?.language = Locale.getDefault()
            } catch (t: Throwable) {}
        }
    }

    private fun resolveLocale(code: String): Locale {
        return when (code.lowercase(Locale.ROOT)) {
            "en" -> Locale.ENGLISH
            "hi" -> LanguageDetector.HINDI_LOCALE
            "ta" -> LanguageDetector.TAMIL_LOCALE
            "te" -> LanguageDetector.TELUGU_LOCALE
            "kn" -> LanguageDetector.KANNADA_LOCALE
            "bn" -> LanguageDetector.BENGALI_LOCALE
            "ar" -> LanguageDetector.ARABIC_LOCALE
            "ur" -> LanguageDetector.URDU_LOCALE
            "ru" -> LanguageDetector.CYRILLIC_LOCALE
            "es" -> LanguageDetector.SPANISH_LOCALE
            "fr" -> LanguageDetector.FRENCH_LOCALE
            "de" -> LanguageDetector.GERMAN_LOCALE
            else -> {
                val parts = code.split("-", "_")
                if (parts.size >= 2) Locale(parts[0], parts[1]) else Locale(code)
            }
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {}
        tts = null
        isReady = false
    }
}

