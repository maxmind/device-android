package com.maxmind.device.collector

import android.content.Context

/**
 * Collects system feature declarations from PackageManager.
 *
 * Retrieves the list of hardware and software features declared
 * by the device manufacturer.
 */
internal class SystemFeaturesCollector(
    private val context: Context,
) {
    /**
     * Collects the list of system features.
     *
     * @return List of feature names available on the device
     */
    fun collect(): List<String> =
        try {
            context.packageManager.systemAvailableFeatures
                .mapNotNull { it.name }
                .sorted()
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // PackageManager may throw on some devices
            emptyList()
        }
}
