package com.kts.textsolution.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent

class KtsMediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
            Log.d(TAG, "onReceive media button: keyCode=${event?.keyCode}, action=${event?.action}")
            if (event != null && event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        if (BackgroundTtsManager.isSpeaking) {
                            Log.d(TAG, "Play/Pause received while speaking -> stopping TTS")
                            BackgroundTtsManager.stop()
                        } else if (BackgroundTtsManager.isPaused) {
                            Log.d(TAG, "Play/Pause received while paused -> resuming TTS")
                            BackgroundTtsManager.resumeOrPlay()
                        }
                    }
                    KeyEvent.KEYCODE_MEDIA_PAUSE,
                    KeyEvent.KEYCODE_MEDIA_STOP -> {
                        Log.d(TAG, "Pause/Stop received -> stopping TTS")
                        BackgroundTtsManager.stop()
                    }
                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        Log.d(TAG, "Play received -> resuming TTS")
                        BackgroundTtsManager.resumeOrPlay()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "KtsMediaButtonReceiver"
    }
}
