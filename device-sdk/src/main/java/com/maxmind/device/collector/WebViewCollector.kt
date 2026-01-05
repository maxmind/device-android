package com.maxmind.device.collector

import android.content.Context
import android.util.Log
import android.webkit.WebSettings

/**
 * Collects WebView context information.
 *
 * Retrieves the default WebView user agent string which provides
 * browser and system version information.
 */
internal class WebViewCollector(
    private val context: Context,
    private val enableLogging: Boolean = false,
) {
    private companion object {
        private const val TAG = "WebViewCollector"
    }

    /**
     * Collects the default WebView user agent.
     *
     * @return User agent string, or null if unavailable
     */
    fun collectUserAgent(): String? =
        try {
            WebSettings.getDefaultUserAgent(context)
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // WebView may not be available on all devices
            if (enableLogging) {
                Log.d(TAG, "Failed to collect WebView user agent: ${e.message}")
            }
            null
        }
}
