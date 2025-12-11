package com.maxmind.device.collector

import android.graphics.Typeface
import com.maxmind.device.model.FontInfo

/**
 * Collects font profile information.
 *
 * Tests for the presence of various system fonts which can help identify
 * device manufacturers and custom ROMs.
 */
internal class FontCollector {
    /**
     * Collects font availability information.
     *
     * @return [FontInfo] containing list of available fonts
     */
    fun collect(): FontInfo {
        val defaultTypeface = Typeface.DEFAULT

        val availableFonts =
            TEST_FONTS.filter { fontFamily ->
                val typeface = Typeface.create(fontFamily, Typeface.NORMAL)
                // A font is considered "available" if it doesn't fall back to default
                // Roboto is always available as it's the Android default
                typeface != defaultTypeface || fontFamily == ROBOTO_FONT
            }

        return FontInfo(availableFonts = availableFonts)
    }

    internal companion object {
        const val ROBOTO_FONT = "Roboto"

        // Common system fonts and manufacturer-specific fonts
        val TEST_FONTS =
            listOf(
                // Standard Android fonts
                ROBOTO_FONT,
                "Noto Sans",
                "Droid Sans",
                "Droid Serif",
                "Droid Sans Mono",
                // Samsung fonts
                "Samsung Sans",
                "SamsungOne",
                // HTC fonts
                "HTC Sense",
                // Sony fonts
                "Sony Sketch",
                "Sony Mobile UD Gothic",
                // LG fonts
                "LG Smart",
                // Xiaomi fonts
                "MIUI",
                "MiSans",
                // OnePlus fonts
                "OnePlus Slate",
                // Google fonts that may be present
                "Google Sans",
                "Product Sans",
            )
    }
}
