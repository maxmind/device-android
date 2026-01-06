package com.maxmind.device.collector

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for FontCollector.
 *
 * Note: Tests that require actual font detection (Typeface.create()) must run
 * in instrumented tests since Android framework classes cannot be mocked in unit tests.
 */
internal class FontCollectorTest {
    @Test
    internal fun `TEST_FONTS contains expected font families`() {
        val fonts = FontCollector.TEST_FONTS

        // Should contain standard Android fonts
        assertTrue(fonts.contains("Roboto"))
        assertTrue(fonts.contains("Noto Sans"))
        assertTrue(fonts.contains("Droid Sans"))

        // Should contain manufacturer-specific fonts
        assertTrue(fonts.contains("Samsung Sans"))
        assertTrue(fonts.contains("OnePlus Slate"))
        assertTrue(fonts.contains("MIUI"))
    }

    @Test
    internal fun `TEST_FONTS is not empty`() {
        assertTrue(FontCollector.TEST_FONTS.isNotEmpty())
    }

    @Test
    internal fun `ROBOTO_FONT constant is Roboto`() {
        assertTrue(FontCollector.ROBOTO_FONT == "Roboto")
    }

    @Test
    internal fun `TEST_FONTS contains ROBOTO_FONT`() {
        assertTrue(FontCollector.TEST_FONTS.contains(FontCollector.ROBOTO_FONT))
    }
}
