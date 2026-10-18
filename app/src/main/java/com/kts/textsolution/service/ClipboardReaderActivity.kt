package com.kts.textsolution.service

import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kts.textsolution.data.ClipboardRepository

class ClipboardReaderActivity : Activity() {

    private var hasRead = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var retryCount = 0
    private val maxRetries = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        hasRead = false
        retryCount = 0
        scheduleClipboardCheck()
    }

    override fun onResume() {
        super.onResume()
        if (!hasRead) {
            scheduleClipboardCheck()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !hasRead) {
            tryReadClipboard()
        }
    }

    private fun scheduleClipboardCheck() {
        if (hasRead) return
        mainHandler.postDelayed({
            if (!hasRead) {
                tryReadClipboard()
                if (!hasRead && retryCount < maxRetries) {
                    retryCount++
                    scheduleClipboardCheck()
                } else if (!hasRead) {
                    finishActivity()
                }
            }
        }, 50)
    }

    private fun tryReadClipboard() {
        if (hasRead) return
        try {
            val clipboard = getSystemService(ClipboardManager::class.java)
            val clip = clipboard?.primaryClip
            val text = ClipboardRepository.extractClipText(clip, this)
            if (text.isNotBlank()) {
                hasRead = true
                Log.d(TAG, "Full clipboard text retrieved on retry $retryCount (length ${text.length}): ${text.take(40)}")
                ClipboardRepository.getInstance(this).addOrUpdateClip(this, text)
                KtsAccessibilityService.notifyClipboardObtained(text)
                BackgroundTtsManager.speak(text, "ClipboardReaderActivity")
                finishActivity()
                return
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error reading clipboard", t)
        }
    }

    private fun finishActivity() {
        mainHandler.removeCallbacksAndMessages(null)
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ClipboardReaderActivity"
    }
}
