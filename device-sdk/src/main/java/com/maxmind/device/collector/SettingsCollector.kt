package com.maxmind.device.collector

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.maxmind.device.model.SystemSettings

/**
 * Collects system settings information.
 *
 * Gathers various system configuration settings that can be useful
 * for device fingerprinting.
 */
internal class SettingsCollector(
    private val context: Context,
    private val enableLogging: Boolean = false,
) {
    private companion object {
        private const val TAG = "SettingsCollector"
    }

    /**
     * Collects system settings.
     *
     * @return [SystemSettings] containing available settings
     */
    fun collect(): SystemSettings =
        SystemSettings(
            screenTimeout = getScreenTimeout(),
            developmentSettingsEnabled = getDevelopmentSettingsEnabled(),
            adbEnabled = getAdbEnabled(),
            animatorDurationScale = getAnimatorDurationScale(),
            bootCount = getBootCount(),
        )

    private fun getScreenTimeout(): Int? =
        try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            if (enableLogging) {
                Log.d(TAG, "Failed to collect screen timeout: ${e.message}")
            }
            null
        }

    private fun getDevelopmentSettingsEnabled(): Boolean? =
        try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            ) == 1
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            if (enableLogging) {
                Log.d(TAG, "Failed to collect development settings enabled: ${e.message}")
            }
            null
        }

    private fun getAdbEnabled(): Boolean? =
        try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED) == 1
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            if (enableLogging) {
                Log.d(TAG, "Failed to collect ADB enabled: ${e.message}")
            }
            null
        }

    private fun getAnimatorDurationScale(): Float? =
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
            )
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            if (enableLogging) {
                Log.d(TAG, "Failed to collect animator duration scale: ${e.message}")
            }
            null
        }

    private fun getBootCount(): Int? =
        try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            if (enableLogging) {
                Log.d(TAG, "Failed to collect boot count: ${e.message}")
            }
            null
        }
}
