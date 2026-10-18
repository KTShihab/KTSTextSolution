package com.kts.textsolution.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.kts.textsolution.MainActivity
import com.kts.textsolution.R
import com.kts.textsolution.data.ClipboardRepository

class KtsBackgroundReaderService : Service() {

    private var clipboardManager: ClipboardManager? = null
    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        BackgroundTtsManager.init(applicationContext)

        val notification = buildNotification(isReading = false, textSnippet = "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        BackgroundTtsManager.onNotificationUpdate = { isReading, textSnippet ->
            val updatedNotif = buildNotification(isReading, textSnippet)
            val nm = getSystemService(NotificationManager::class.java)
            nm?.notify(NOTIFICATION_ID, updatedNotif)
        }

        setupClipboardListener()
    }

    private fun setupClipboardListener() {
        clipboardManager = getSystemService(ClipboardManager::class.java)
        clipListener = ClipboardManager.OnPrimaryClipChangedListener {
            try {
                val clip = clipboardManager?.primaryClip ?: return@OnPrimaryClipChangedListener
                val text = ClipboardRepository.extractClipText(clip, this)
                if (text.isNotBlank()) {
                    ClipboardRepository.getInstance(this).addOrUpdateClip(this, text)
                    if (BackgroundTtsManager.isBackgroundReadingEnabled(this)) {
                        BackgroundTtsManager.speak(text, "ServiceClipboardListener")
                    }
                }
            } catch (t: Throwable) {
                // Handled gracefully for large parcels / restrictions
            }
        }
        clipboardManager?.addPrimaryClipChangedListener(clipListener)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (BackgroundTtsManager.isBackgroundReadingEnabled(this)) {
            val restartIntent = Intent(applicationContext, KtsBackgroundReaderService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_READING -> {
                BackgroundTtsManager.stop()
            }
            ACTION_TEST_SPEAK -> {
                val text = intent.getStringExtra("text") ?: "KTS Text Solution reading started."
                BackgroundTtsManager.speak(text, "TestIntent")
            }
            ACTION_STOP_SERVICE -> {
                BackgroundTtsManager.stop()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun buildNotification(isReading: Boolean, textSnippet: String): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val stopIntent = Intent(this, KtsBackgroundReaderService::class.java).apply {
            action = ACTION_STOP_READING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val title = if (isReading) {
            getString(R.string.notification_reading_now, textSnippet)
        } else {
            getString(R.string.notification_bg_reader_title)
        }

        val text = if (isReading) {
            textSnippet
        } else {
            getString(R.string.notification_bg_reader_text)
        }

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)

        val sessionToken = KtsMediaSessionManager.getSessionToken()
        if (isReading && sessionToken != null) {
            val stopAction = Notification.Action.Builder(
                android.R.drawable.ic_media_pause,
                getString(R.string.action_stop_reading),
                stopPendingIntent,
            ).build()
            builder.addAction(stopAction)

            val mediaStyle = Notification.MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0)
            builder.setStyle(mediaStyle)
        } else if (isReading) {
            val stopAction = Notification.Action.Builder(
                android.R.drawable.ic_media_pause,
                getString(R.string.action_stop_reading),
                stopPendingIntent,
            ).build()
            builder.addAction(stopAction)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_bg_reader)
            val descriptionText = getString(R.string.notification_channel_bg_reader_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        clipListener?.let {
            clipboardManager?.removePrimaryClipChangedListener(it)
        }
        BackgroundTtsManager.onNotificationUpdate = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "kts_bg_reader_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.kts.textsolution.action.START"
        const val ACTION_STOP_SERVICE = "com.kts.textsolution.action.STOP_SERVICE"
        const val ACTION_STOP_READING = "com.kts.textsolution.action.STOP_READING"
        const val ACTION_TEST_SPEAK = "com.kts.textsolution.action.TEST_SPEAK"

        fun start(context: Context) {
            val intent = Intent(context, KtsBackgroundReaderService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KtsBackgroundReaderService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                context.stopService(intent)
            }
        }
    }
}

