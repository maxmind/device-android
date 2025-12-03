package com.maxmind.device.collector

import android.content.Context
import android.webkit.WebSettings

/**
 * Collects WebView context information.
 *
 * Retrieves the default WebView user agent string which provides
 * browser and system version information.
 */
internal class WebViewCollector(
    private val context: Context,
) {
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
            null
        }
}
