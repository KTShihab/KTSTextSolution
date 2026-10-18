package com.kts.textsolution.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.kts.textsolution.data.ClipboardRepository
import com.kts.textsolution.data.KtsPreferences
import java.util.Locale

class KtsAccessibilityService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var clipboardManager: ClipboardManager? = null
    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var lastInteractedText: String = ""
    private var lastHandledText: String = ""
    private var pendingFallbackRunnable: Runnable? = null
    private var lastAutoExpandTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "KtsAccessibilityService onServiceConnected")
        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.notificationTimeout = 100
            serviceInfo = info

            BackgroundTtsManager.init(applicationContext)

            // Auto-enable and elevate to Foreground Service
            KtsPreferences.setAutoReadBackgroundEnabled(this, true)
            KtsBackgroundReaderService.start(this)

            clipboardManager = getSystemService(ClipboardManager::class.java)
            clipListener = ClipboardManager.OnPrimaryClipChangedListener {
                try {
                    val clip = clipboardManager?.primaryClip ?: return@OnPrimaryClipChangedListener
                    val text = ClipboardRepository.extractClipText(clip, this)
                    if (text.isNotBlank() && text != lastHandledText) {
                        lastHandledText = text
                        Log.d(TAG, "Clip changed listener detected text: ${text.take(30)}")
                        ClipboardRepository.getInstance(this).addOrUpdateClip(this, text)
                        BackgroundTtsManager.speak(text, "AccessibilityClipboardListener")
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Error in primaryClipChanged listener: ${t.message}")
                }
            }
            clipboardManager?.addPrimaryClipChangedListener(clipListener)
        } catch (t: Throwable) {
            Log.e(TAG, "Error in onServiceConnected", t)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                    val fullText = event.text?.joinToString(" ") { it.toString() } ?: ""
                    val from = event.fromIndex
                    val to = event.toIndex
                    val selectedText = if (fullText.isNotBlank() && from >= 0 && to > from && to <= fullText.length) {
                        fullText.substring(from, to).trim()
                    } else {
                        extractFullNodeText(event.source)?.trim() ?: fullText.trim()
                    }
                    if (selectedText.isNotBlank()) {
                        lastInteractedText = selectedText
                    }
                }
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                    val eventListText = event.text?.joinToString("\n") { it.toString() }?.trim() ?: ""
                    val nodeText = extractFullNodeText(event.source)?.trim() ?: ""
                    val candidate = if (nodeText.isNotBlank()) nodeText else eventListText
                    if (candidate.isNotBlank()) {
                        lastInteractedText = candidate
                    }
                    if (isTruncatedText(candidate)) {
                        expandReadMore(event.source, rootInActiveWindow)
                        mainHandler.postDelayed({
                            val expanded = findExpandedNodeText(candidate, rootInActiveWindow)
                            if (!expanded.isNullOrBlank() && expanded.length > candidate.length) {
                                lastInteractedText = expanded
                            }
                        }, 120)
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    val pkg = event.packageName?.toString() ?: ""
                    if (pkg.contains("whatsapp", ignoreCase = true)) {
                        val now = System.currentTimeMillis()
                        if (now - lastAutoExpandTime > 600) {
                            lastAutoExpandTime = now
                            expandReadMore(event.source, rootInActiveWindow)
                        }
                    }
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    val desc = event.contentDescription?.toString() ?: ""
                    val text = event.text?.joinToString(" ") { it.toString() } ?: ""
                    val viewId = try { event.source?.viewIdResourceName ?: "" } catch (e: Throwable) { "" }

                    if (isCopyTrigger(desc, text, viewId)) {
                        Log.d(TAG, "Copy button clicked! desc=$desc, text=$text, viewId=$viewId")
                        mainHandler.postDelayed({
                            handleCopyAction()
                        }, 120)
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error in onAccessibilityEvent", t)
        }
    }

    private fun isCopyTrigger(desc: String, text: String, viewId: String): Boolean {
        val lowerDesc = desc.lowercase(Locale.ROOT)
        val lowerText = text.lowercase(Locale.ROOT)
        val lowerId = viewId.lowercase(Locale.ROOT)

        // Never trigger on close / cancel / clear / back buttons
        if (lowerId.contains("close") || lowerId.contains("cancel") || lowerId.contains("back") || lowerId.contains("clear")) {
            return false
        }

        // Multi-language copy terms
        val copyTerms = listOf(
            "copy",
            "പകർത്തുക", "കോപ്പി", "പകർപ്പ്",
            "कॉपी", "प्रतिलिपि",
            "நகலெடு",
            "కాపీ", "నకలు",
            "ನಕಲಿಸು",
            "نسخ",
            "copiar", "copie", "copier", "kopieren", "копировать", "salin", "sao chép"
        )

        val matchDesc = copyTerms.any { lowerDesc.contains(it) }
        val matchText = copyTerms.any { lowerText.contains(it) }
        val matchId = lowerId.contains("copy") ||
                lowerId.contains("menuitem_copy") ||
                lowerId.contains("action_copy") ||
                lowerId.contains("btn_copy") ||
                lowerId.contains("button_copy") ||
                lowerId.endsWith(":id/copy")

        return matchDesc || matchText || matchId
    }

    private fun isTruncatedText(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        val readMoreIndicators = listOf(
            "read more", "readmore",
            "കൂടുതൽ വായിക്കുക",
            "leer más", "leermas",
            "और पढ़ें",
            "see more", "seemore"
        )
        if (readMoreIndicators.any { lower.contains(it) }) {
            return true
        }
        val trimmed = text.trim()
        return trimmed.endsWith("…") || trimmed.endsWith("...") || trimmed.endsWith("..")
    }

    fun onClipboardObtained(fullText: String) {
        pendingFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingFallbackRunnable = null
        lastHandledText = fullText
        lastInteractedText = ""
    }

    private fun handleCopyAction() {
        try {
            // 1. Try reading directly from clipboardManager (Android 9 and below or focused window)
            var directText: String? = null
            try {
                val clip = clipboardManager?.primaryClip
                directText = ClipboardRepository.extractClipText(clip, this)
            } catch (t: Throwable) {
                // Background clipboard restriction on Android 10+
            }

            if (!directText.isNullOrBlank() && directText != lastHandledText) {
                pendingFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
                pendingFallbackRunnable = null
                lastHandledText = directText
                ClipboardRepository.getInstance(this).addOrUpdateClip(this, directText)
                BackgroundTtsManager.speak(directText, "AccessibilityDirectClipboard")
                return
            }

            val fallbackText = lastInteractedText.trim()
            val isTruncated = isTruncatedText(fallbackText)

            // Cancel previous pending fallback runnable
            pendingFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
            pendingFallbackRunnable = null

            if (isTruncated) {
                Log.d(TAG, "Truncated message on copy! Auto-expanding in WhatsApp...")
                expandReadMore(null, rootInActiveWindow)

                mainHandler.postDelayed({
                    val expanded = findExpandedNodeText(fallbackText, rootInActiveWindow)
                    val textToSpeak = if (!expanded.isNullOrBlank() && expanded.length > fallbackText.length) {
                        Log.d(TAG, "Successfully expanded message on copy! length=${expanded.length}")
                        expanded
                    } else {
                        cleanPrefix(fallbackText)
                    }

                    if (textToSpeak.isNotBlank() && textToSpeak != lastHandledText) {
                        lastHandledText = textToSpeak
                        ClipboardRepository.getInstance(this).addOrUpdateClip(this, textToSpeak)
                        BackgroundTtsManager.speak(textToSpeak, "WhatsAppExpandedNode")
                    }
                }, 140)

                // Also try launching ClipboardReaderActivity in background just in case
                launchClipboardReaderActivity()
                return
            }

            // Launch ClipboardReaderActivity to attempt reading full clipboard
            launchClipboardReaderActivity()

            // If not truncated, speak fallbackText immediately
            if (fallbackText.isNotBlank() && fallbackText != lastHandledText) {
                lastHandledText = fallbackText
                ClipboardRepository.getInstance(this).addOrUpdateClip(this, fallbackText)
                BackgroundTtsManager.speak(fallbackText, "AccessibilityTree")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error in handleCopyAction", t)
        }
    }

    private fun cleanPrefix(text: String): String {
        var s = text.trim()
        val suffixes = listOf(
            "... Read more", "...Read more", "… Read more", "…Read more", "Read more", "Read More",
            "… കൂടുതൽ വായിക്കുക", "... കൂടുതൽ വായിക്കുക", "കൂടുതൽ വായിക്കുക",
            "… See more", "... See more", "See more",
            "… Leer más", "... Leer más", "Leer más",
            "… और पढ़ें", "... और पढ़ें", "और पढ़ें",
            "…", "..."
        )
        for (suffix in suffixes) {
            if (s.endsWith(suffix, ignoreCase = true)) {
                s = s.substring(0, s.length - suffix.length).trimEnd()
            }
        }
        return s
    }

    private fun findExpandedNodeText(truncatedText: String, rootNode: AccessibilityNodeInfo?): String? {
        val prefix = cleanPrefix(truncatedText)
        if (prefix.length < 10) return null

        val root = rootNode ?: rootInActiveWindow ?: return null
        val texts = mutableListOf<String>()
        collectAllTexts(root, texts, 0)

        val searchPrefix = prefix.take(minOf(40, prefix.length))
        return texts
            .filter { it.length > truncatedText.length }
            .firstOrNull { it.contains(searchPrefix) || it.startsWith(searchPrefix) }
    }

    private fun collectAllTexts(node: AccessibilityNodeInfo?, result: MutableList<String>, depth: Int) {
        if (node == null || depth > 8) return
        val t = node.text?.toString()?.trim()
        if (!t.isNullOrBlank() && !result.contains(t)) {
            result.add(t)
        }
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (e: Throwable) { null }
            collectAllTexts(child, result, depth + 1)
        }
    }

    private fun expandReadMore(sourceNode: AccessibilityNodeInfo?, rootNode: AccessibilityNodeInfo?): Boolean {
        var expanded = false
        val terms = listOf("Read more", "Read More", "read more", "കൂടുതൽ വായിക്കുക", "See more", "Leer más", "और पढ़ें")

        val candidates = mutableListOf<AccessibilityNodeInfo>()
        val root = rootNode ?: rootInActiveWindow

        if (root != null) {
            for (term in terms) {
                try {
                    val list = root.findAccessibilityNodeInfosByText(term)
                    if (!list.isNullOrEmpty()) {
                        candidates.addAll(list)
                    }
                } catch (t: Throwable) {}
            }
        }

        if (sourceNode != null) {
            collectMatchingNodes(sourceNode, candidates, terms, 0)
        }

        for (node in candidates) {
            try {
                var target: AccessibilityNodeInfo? = node
                var depth = 0
                while (target != null && !target.isClickable && depth < 5) {
                    target = target.parent
                    depth++
                }
                if (target != null && target.isClickable) {
                    val res = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (res) expanded = true
                } else {
                    val res = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (res) expanded = true
                }
            } catch (t: Throwable) {}
        }

        return expanded
    }

    private fun collectMatchingNodes(node: AccessibilityNodeInfo?, result: MutableList<AccessibilityNodeInfo>, terms: List<String>, depth: Int) {
        if (node == null || depth > 6) return
        val t = node.text?.toString()?.lowercase(Locale.ROOT) ?: ""
        val d = node.contentDescription?.toString()?.lowercase(Locale.ROOT) ?: ""
        if (terms.any { t.contains(it.lowercase(Locale.ROOT)) || d.contains(it.lowercase(Locale.ROOT)) }) {
            if (!result.contains(node)) {
                result.add(node)
            }
        }
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (e: Throwable) { null }
            collectMatchingNodes(child, result, terms, depth + 1)
        }
    }

    private fun launchClipboardReaderActivity() {
        try {
            val intent = Intent(this, ClipboardReaderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic()
                try {
                    options.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                } catch (t: Throwable) {}
                val bundle = options.toBundle()

                val pi = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    bundle,
                )
                try {
                    pi.send(this, 0, null, null, null, null, bundle)
                } catch (t: Throwable) {
                    try {
                        startActivity(intent, bundle)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Failed to start ClipboardReaderActivity with bundle", e)
                    }
                }
            } else {
                startActivity(intent)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error launching ClipboardReaderActivity", t)
        }
    }

    private fun extractFullNodeText(node: AccessibilityNodeInfo?, depth: Int = 0): String? {
        if (node == null || depth > 6) return null
        val texts = mutableListOf<String>()
        collectTexts(node, texts, 0)
        if (texts.isEmpty()) return null
        if (texts.size == 1) return texts.first()

        // Filter out standalone timestamps ("10:30 am", "10:30")
        val contentTexts = texts.filter { str ->
            val trimmed = str.trim()
            !trimmed.matches(Regex("^(?:\\d{1,2}:\\d{2}(?:\\s*(?:am|pm|AM|PM))?|yesterday|today|edited)$", RegexOption.IGNORE_CASE))
        }

        return if (contentTexts.isNotEmpty()) {
            contentTexts.joinToString("\n").trim()
        } else {
            texts.joinToString(" ").trim()
        }
    }

    private fun collectTexts(node: AccessibilityNodeInfo?, result: MutableList<String>, depth: Int) {
        if (node == null || depth > 6) return
        val nodeText = node.text?.toString()?.trim()
        if (!nodeText.isNullOrBlank() && !result.contains(nodeText)) {
            result.add(nodeText)
        }
        val count = node.childCount
        for (i in 0 until count) {
            val child = try { node.getChild(i) } catch (t: Throwable) { null }
            collectTexts(child, result, depth + 1)
        }
    }

    override fun onInterrupt() {
        try {
            BackgroundTtsManager.stop()
        } catch (t: Throwable) {
            Log.e(TAG, "Error in onInterrupt", t)
        }
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        pendingFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingFallbackRunnable = null
        try {
            clipListener?.let {
                clipboardManager?.removePrimaryClipChangedListener(it)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error in onDestroy", t)
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "KtsA11yService"
        var instance: KtsAccessibilityService? = null
            private set

        fun notifyClipboardObtained(text: String) {
            instance?.onClipboardObtained(text)
        }
    }
}
