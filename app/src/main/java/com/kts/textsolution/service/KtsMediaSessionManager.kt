package com.kts.textsolution.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent

object KtsMediaSessionManager {
    private const val TAG = "KtsMediaSession"

    private var appContext: Context? = null
    private var mediaSession: MediaSession? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var mbrPendingIntent: PendingIntent? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    var onPlayRequested: (() -> Unit)? = null
    var onPauseRequested: (() -> Unit)? = null
    var onStopRequested: (() -> Unit)? = null

    private var isPlayingState: Boolean = false
    private var isPausedState: Boolean = false

    private val pauseTimeoutRunnable = Runnable {
        Log.d(TAG, "Pause timeout reached, deactivating media session")
        setStopped()
        onStopRequested?.invoke()
    }

    val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    fun init(context: Context) {
        if (mediaSession != null) return
        val app = context.applicationContext
        appContext = app
        audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        try {
            val session = MediaSession(app, "KTSTextSolutionMediaSession")
            session.setFlags(
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Register explicit MediaButtonReceiver for system & Vivo/OEM media button routing
            val mbrIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, app, KtsMediaButtonReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                app,
                0,
                mbrIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            mbrPendingIntent = pi
            session.setMediaButtonReceiver(pi)

            session.setCallback(object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                    val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    }

                    Log.d(TAG, "MediaSession.Callback onMediaButtonEvent: keyCode=${event?.keyCode}, action=${event?.action}")
                    if (event != null && event.action == KeyEvent.ACTION_DOWN) {
                        when (event.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                            KeyEvent.KEYCODE_HEADSETHOOK -> {
                                if (isPlayingState) {
                                    handlePause()
                                } else {
                                    handlePlay()
                                }
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                handlePause()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_STOP -> {
                                handleStop()
                                return true
                            }
                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                handlePlay()
                                return true
                            }
                        }
                    }
                    return super.onMediaButtonEvent(mediaButtonIntent)
                }

                override fun onPlay() {
                    Log.d(TAG, "MediaSession callback onPlay()")
                    handlePlay()
                }

                override fun onPause() {
                    Log.d(TAG, "MediaSession callback onPause()")
                    handlePause()
                }

                override fun onStop() {
                    Log.d(TAG, "MediaSession callback onStop()")
                    handleStop()
                }
            }, mainHandler)

            val initState = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY_PAUSE or
                            PlaybackState.ACTION_PLAY
                )
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0.0f)
                .build()
            session.setPlaybackState(initState)
            session.isActive = true

            mediaSession = session
            Log.d(TAG, "MediaSession initialized with MediaButtonReceiver successfully, isActive=true")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize MediaSession", t)
        }
    }

    private fun handlePlay() {
        mainHandler.post {
            onPlayRequested?.invoke()
        }
    }

    private fun handlePause() {
        mainHandler.post {
            onPauseRequested?.invoke()
        }
    }

    private fun handleStop() {
        mainHandler.post {
            onStopRequested?.invoke()
        }
    }

    fun getSessionToken(): MediaSession.Token? = mediaSession?.sessionToken

    fun setPlaying(title: String = "KTS Text Reader", snippet: String = "") {
        mainHandler.removeCallbacks(pauseTimeoutRunnable)
        requestAudioFocus()

        try {
            val session = mediaSession ?: return
            mbrPendingIntent?.let { session.setMediaButtonReceiver(it) }

            val metadata = MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, snippet.ifBlank { title })
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "KTS Text Solution")
                .build()
            session.setMetadata(metadata)

            val state = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY_PAUSE or
                            PlaybackState.ACTION_PAUSE or
                            PlaybackState.ACTION_STOP or
                            PlaybackState.ACTION_PLAY
                )
                .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()

            session.setPlaybackState(state)
            session.isActive = true
            isPlayingState = true
            isPausedState = false
            Log.d(TAG, "MediaSession set to PLAYING, isActive=true")
        } catch (t: Throwable) {
            Log.e(TAG, "Error setting MediaSession to PLAYING", t)
        }
    }

    fun setPaused(timeoutMs: Long = 45000L) {
        isPlayingState = false
        isPausedState = true
        abandonAudioFocus()

        try {
            val session = mediaSession ?: return
            val state = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY_PAUSE or
                            PlaybackState.ACTION_PLAY or
                            PlaybackState.ACTION_STOP
                )
                .setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0.0f)
                .build()

            session.setPlaybackState(state)
            // Keep session active during pause so user can 2-finger double tap to resume
            session.isActive = true
            Log.d(TAG, "MediaSession set to PAUSED, isActive=true")

            mainHandler.removeCallbacks(pauseTimeoutRunnable)
            if (timeoutMs > 0) {
                mainHandler.postDelayed(pauseTimeoutRunnable, timeoutMs)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error setting MediaSession to PAUSED", t)
        }
    }

    fun setStopped() {
        mainHandler.removeCallbacks(pauseTimeoutRunnable)
        isPlayingState = false
        isPausedState = false
        abandonAudioFocus()

        try {
            val session = mediaSession ?: return
            val state = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY_PAUSE or
                            PlaybackState.ACTION_PLAY
                )
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0.0f)
                .build()

            session.setPlaybackState(state)
            session.isActive = true
            Log.d(TAG, "MediaSession set to STOPPED, isActive=true")
        } catch (t: Throwable) {
            Log.e(TAG, "Error setting MediaSession to STOPPED", t)
        }
    }

    private fun requestAudioFocus() {
        try {
            val am = audioManager ?: return
            if (audioFocusRequest == null) {
                // AUDIOFOCUS_GAIN ensures system sets KTSTextSolution as primary MediaButtonReceiver
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener({ focusChange ->
                        Log.d(TAG, "AudioFocus changed: $focusChange")
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                            focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        ) {
                            handlePause()
                        }
                    }, mainHandler)
                    .build()
            }
            audioFocusRequest?.let {
                val res = am.requestAudioFocus(it)
                Log.d(TAG, "requestAudioFocus result: $res")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to request audio focus: ${t.message}")
        }
    }

    private fun abandonAudioFocus() {
        try {
            val am = audioManager ?: return
            audioFocusRequest?.let {
                am.abandonAudioFocusRequest(it)
                Log.d(TAG, "abandonAudioFocus abandoned")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to abandon audio focus: ${t.message}")
        }
    }

    fun release() {
        mainHandler.removeCallbacks(pauseTimeoutRunnable)
        abandonAudioFocus()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
            mediaSession = null
        } catch (t: Throwable) {
            Log.w(TAG, "Error releasing MediaSession: ${t.message}")
        }
        appContext = null
        audioManager = null
        audioFocusRequest = null
        mbrPendingIntent = null
    }
}
