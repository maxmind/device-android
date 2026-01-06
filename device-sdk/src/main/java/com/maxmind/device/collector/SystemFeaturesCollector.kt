package com.maxmind.device.collector

import android.content.Context
import android.util.Log

/**
 * Collects system feature declarations from PackageManager.
 *
 * Retrieves the list of hardware and software features declared
 * by the device manufacturer.
 */
internal class SystemFeaturesCollector(
    private val context: Context,
    private val enableLogging: Boolean = false,
) {
    private companion object {
        private const val TAG = "SystemFeaturesCollector"
    }

    /**
     * Collects the list of system features.
     *
     * @return List of feature names available on the device
     */
    fun collect(): List<String> =
        try {
            // Returns features in the order provided by the system - this order
            // is deterministic/stable on a given device per the Android HAL spec
            context.packageManager.systemAvailableFeatures
                .mapNotNull { it.name }
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // PackageManager may throw on some devices
            if (enableLogging) {
                Log.d(TAG, "Failed to collect system features: ${e.message}")
            }
            emptyList()
        }
}
