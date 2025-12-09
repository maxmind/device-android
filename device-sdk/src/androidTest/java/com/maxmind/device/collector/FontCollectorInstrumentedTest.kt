package com.maxmind.device.collector

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [FontCollector].
 *
 * These tests run on an Android device/emulator to verify font detection
 * using real Android Typeface API.
 */
@RunWith(AndroidJUnit4::class)
public class FontCollectorInstrumentedTest {
    @Test
    public fun collect_returnsFontInfo() {
        val collector = FontCollector()

        val result = collector.collect()

        assertNotNull("Font info should not be null", result)
        assertNotNull("Available fonts list should not be null", result.availableFonts)
    }

    @Test
    public fun collect_detectsRobotoFont() {
        val collector = FontCollector()

        val result = collector.collect()

        // Roboto is always available as the default Android font
        // FontCollector only tests fonts from its TEST_FONTS list
        assertTrue(
            "Roboto font should be available (Android default)",
            result.availableFonts.contains("Roboto"),
        )
    }

    @Test
    public fun collect_returnsNonEmptyFontList() {
        val collector = FontCollector()

        val result = collector.collect()

        assertTrue(
            "Available fonts should not be empty",
            result.availableFonts.isNotEmpty(),
        )
    }

    @Test
    public fun collect_isConsistentAcrossMultipleCalls() {
        val collector = FontCollector()

        val result1 = collector.collect()
        val result2 = collector.collect()

        assertTrue(
            "Font list should be consistent across calls",
            result1.availableFonts == result2.availableFonts,
        )
    }
}
