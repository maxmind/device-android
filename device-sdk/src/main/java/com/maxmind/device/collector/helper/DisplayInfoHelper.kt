package com.maxmind.device.collector.helper

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import com.maxmind.device.model.DisplayInfo

/**
 * Helper class for collecting display information.
 *
 * Encapsulates access to [DisplayManager] for testability.
 */
internal class DisplayInfoHelper(
    private val context: Context,
) {
    /**
     * Collects display information from the device.
     *
     * @return [DisplayInfo] containing display metrics, or null if unavailable
     */
    @Suppress("DEPRECATION")
    public fun collect(): DisplayInfo? {
        val displayManager =
            context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                ?: return null

        val display =
            displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                ?: return null

        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)

        // Get refresh rate using modern API on Android R+
        val refreshRate =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display.mode.refreshRate
            } else {
                display.refreshRate
            }

        // Collect HDR capabilities on Android N+ (API 24)
        val hdrCapabilities =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                display.hdrCapabilities?.supportedHdrTypes?.toList()
            } else {
                null
            }

        return DisplayInfo(
            widthPixels = displayMetrics.widthPixels,
            heightPixels = displayMetrics.heightPixels,
            densityDpi = displayMetrics.densityDpi,
            density = displayMetrics.density,
            refreshRate = refreshRate,
            hdrCapabilities = hdrCapabilities,
        )
    }
}
