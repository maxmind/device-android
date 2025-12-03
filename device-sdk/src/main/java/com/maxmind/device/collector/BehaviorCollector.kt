package com.maxmind.device.collector

import android.content.Context
import android.provider.Settings
import com.maxmind.device.model.BehaviorInfo

/**
 * Collects behavioral signals from user configuration.
 *
 * Gathers information about enabled input methods and accessibility services
 * which can indicate device usage patterns.
 */
internal class BehaviorCollector(
    private val context: Context,
) {
    /**
     * Collects behavioral information.
     *
     * @return [BehaviorInfo] containing enabled services
     */
    fun collect(): BehaviorInfo =
        BehaviorInfo(
            enabledKeyboards = getEnabledKeyboards(),
            enabledAccessibilityServices = getEnabledAccessibilityServices(),
        )

    private fun getEnabledKeyboards(): List<String> =
        try {
            val enabledInputMethods =
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_INPUT_METHODS,
                )
            enabledInputMethods?.split(":")?.filter { it.isNotBlank() } ?: emptyList()
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            emptyList()
        }

    private fun getEnabledAccessibilityServices(): List<String> =
        try {
            val enabledServices =
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                )
            enabledServices?.split(":")?.filter { it.isNotBlank() } ?: emptyList()
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            emptyList()
        }
}
