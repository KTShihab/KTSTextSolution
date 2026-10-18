package com.kts.textsolution.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.kts.textsolution.data.KtsPreferences

object KtsHaptics {

    /**
     * Tactile feedback for long-press / hold gestures.
     */
    fun vibrateLongPress(context: Context) {
        if (!KtsPreferences.isHapticFeedbackEnabled(context)) return
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.EFFECT_HEAVY_CLICK
        } else null
        vibrate(context, durationMs = 50, effectPredefined = effect)
    }

    /**
     * Crisp tactile tick for selecting / toggling checkboxes / chunk selection.
     */
    fun vibrateSelect(context: Context) {
        if (!KtsPreferences.isHapticFeedbackEnabled(context)) return
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.EFFECT_TICK
        } else null
        vibrate(context, durationMs = 25, effectPredefined = effect)
    }

    /**
     * Tactile feedback when executing an action from a menu, sheet, or TalkBack action.
     */
    fun vibrateAction(context: Context) {
        if (!KtsPreferences.isHapticFeedbackEnabled(context)) return
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.EFFECT_CLICK
        } else null
        vibrate(context, durationMs = 35, effectPredefined = effect)
    }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context, durationMs: Long, effectPredefined: Int?) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && effectPredefined != null) {
                vibrator.vibrate(VibrationEffect.createPredefined(effectPredefined))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore if vibration is not permitted or unavailable
        }
    }
}
