package com.kts.textsolution

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.kts.textsolution.data.ClipboardRepository
import com.kts.textsolution.data.KtsPreferences
import com.kts.textsolution.service.BackgroundTtsManager
import com.kts.textsolution.service.KtsBackgroundReaderService
import com.kts.textsolution.ui.KtsApp
import com.kts.textsolution.ui.theme.KtsTheme

class MainActivity : ComponentActivity() {
    private val vm: KtsViewModel by viewModels()
    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted && vm.autoReadInBackground) {
            KtsBackgroundReaderService.start(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BackgroundTtsManager.init(this)
        BackgroundTtsManager.onStateChange = { speaking ->
            vm.isSpeaking = speaking
        }

        vm.initSettings(this)

        if (vm.autoReadInBackground) {
            checkAndRequestNotificationPermission()
            KtsBackgroundReaderService.start(this)
        }

        setContent {
            KtsTheme(darkTheme = vm.darkTheme, highContrast = vm.highContrast) {
                LaunchedEffect(Unit) {
                    repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        readClipboard(auto = true)
                    }
                }
                KtsApp(
                    vm = vm,
                    onCopy = {
                        val text = vm.copyText()
                        if (text.isBlank()) {
                            Toast.makeText(this, getString(R.string.empty_title), Toast.LENGTH_SHORT).show()
                        } else {
                            clipboard().setPrimaryClip(ClipData.newPlainText("KTS", text))
                            ClipboardRepository.getInstance(this).addOrUpdateClip(this, text)
                            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
                            vm.announce = getString(R.string.copied)
                        }
                    },
                    onPaste = { readClipboard(auto = false) },
                    onPlay = {
                        if (vm.isSpeaking) {
                            BackgroundTtsManager.stop()
                            vm.announce = getString(R.string.tts_stopped)
                        } else {
                            val text = vm.textToPlay()
                            if (text.isBlank()) {
                                Toast.makeText(this, getString(R.string.empty_title), Toast.LENGTH_SHORT).show()
                                vm.announce = getString(R.string.empty_title)
                            } else {
                                BackgroundTtsManager.speak(text)
                                vm.announce = getString(R.string.tts_playing)
                            }
                        }
                    },
                    onSpeakChunk = { chunkText ->
                        if (chunkText.isNotBlank()) {
                            BackgroundTtsManager.speak(chunkText)
                            vm.announce = getString(R.string.tts_playing)
                        }
                    },
                    onToggleBackgroundReading = { enabled ->
                        vm.setAutoReadBackground(
                            context = this,
                            enabled = enabled,
                            onStartService = {
                                checkAndRequestNotificationPermission()
                                KtsBackgroundReaderService.start(this)
                            },
                            onStopService = {
                                KtsBackgroundReaderService.stop(this)
                            },
                        )
                    },
                    onOpenAccessibilitySettings = {
                        KtsPreferences.openAccessibilitySettings(this)
                    },
                    onRequestIgnoreBatteryOptimization = {
                        KtsPreferences.requestIgnoreBatteryOptimization(this)
                    },
                )
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshPermissionStates(this)
        setupForegroundClipListener()
    }

    override fun onPause() {
        super.onPause()
        removeForegroundClipListener()
        if (!vm.autoReadInBackground) {
            BackgroundTtsManager.stop()
        }
    }

    override fun onDestroy() {
        if (!vm.autoReadInBackground) {
            BackgroundTtsManager.shutdown()
        }
        super.onDestroy()
    }

    private fun setupForegroundClipListener() {
        if (clipListener == null) {
            clipListener = ClipboardManager.OnPrimaryClipChangedListener {
                val clip = clipboard().primaryClip ?: return@OnPrimaryClipChangedListener
                val text = ClipboardRepository.extractClipText(clip, this)
                if (text.isNotBlank()) {
                    ClipboardRepository.getInstance(this).addOrUpdateClip(this, text)
                }
            }
            clipboard().addPrimaryClipChangedListener(clipListener)
        }
    }

    private fun removeForegroundClipListener() {
        clipListener?.let {
            clipboard().removePrimaryClipChangedListener(it)
            clipListener = null
        }
    }

    private fun clipboard(): ClipboardManager =
        getSystemService(ClipboardManager::class.java)

    private fun readClipboard(auto: Boolean) {
        if (auto && !vm.autoClipboard) return
        val clip = clipboard().primaryClip ?: return
        val text = ClipboardRepository.extractClipText(clip, this)
        if (text.isBlank()) return
        ClipboardRepository.getInstance(this).addOrUpdateClip(this, text)
        val imported = getString(R.string.imported)
        val loading = getString(R.string.loading_web_page)
        val webSuccess = getString(R.string.web_page_imported)
        val webError = getString(R.string.web_page_error)
        if (auto) {
            vm.considerClipboard(text, imported, loading, webSuccess, webError)
        } else {
            vm.importOrFetch(text, true, imported, loading, webSuccess, webError)
        }
    }
}

