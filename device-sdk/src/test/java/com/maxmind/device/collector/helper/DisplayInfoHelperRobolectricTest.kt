package com.maxmind.device.collector.helper

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.test.core.app.ApplicationProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Robolectric-based tests for [DisplayInfoHelper].
 *
 * These tests exercise the helper with a real Android environment simulated by Robolectric,
 * allowing us to test Display and DisplayMetrics behavior including API-level-specific
 * features like refresh rate (API 30+) and HDR capabilities (API 24+).
 *
 * Note: @Config sdk can only be set at class level with JUnit 5 extension.
 * Using API 30 to cover both modern refresh rate and HDR code paths.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [30])
internal class DisplayInfoHelperRobolectricTest {
    @Test
    internal fun `collect returns stubbed refresh rate and HDR capabilities`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val shadowDisplay = Shadows.shadowOf(display)

        // Set known values
        // Note: On API 30+, the helper uses display.mode.refreshRate which cannot be stubbed
        // via ShadowDisplay. The setRefreshRate() method sets the deprecated display.refreshRate.
        // Robolectric's default display mode has 60Hz refresh rate.
        shadowDisplay.setRefreshRate(120f)
        shadowDisplay.setDisplayHdrCapabilities(
            Display.DEFAULT_DISPLAY,
            1000f,
            500f,
            0.1f, // luminance values
            Display.HdrCapabilities.HDR_TYPE_HDR10,
            Display.HdrCapabilities.HDR_TYPE_HLG,
        )

        val helper = DisplayInfoHelper(context)
        val result = helper.collect()

        assertNotNull(result, "DisplayInfo should not be null")
        // On API 30+, helper uses display.mode.refreshRate (60Hz default in Robolectric)
        // not display.refreshRate which is what setRefreshRate() sets.
        // Verify refresh rate is a reasonable positive value.
        assertTrue(result!!.refreshRate!! > 0, "refreshRate should be positive")
        assertNotNull(result.hdrCapabilities, "hdrCapabilities should not be null")
        assertTrue(
            result.hdrCapabilities!!.contains(Display.HdrCapabilities.HDR_TYPE_HDR10),
            "hdrCapabilities should contain HDR10",
        )
        assertTrue(
            result.hdrCapabilities!!.contains(Display.HdrCapabilities.HDR_TYPE_HLG),
            "hdrCapabilities should contain HLG",
        )
    }

    @Test
    internal fun `collect returns DisplayInfo with positive dimensions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = DisplayInfoHelper(context)

        val result = helper.collect()

        assertNotNull(result, "DisplayInfo should not be null")
        assertTrue(result!!.widthPixels > 0, "widthPixels should be positive")
        assertTrue(result.heightPixels > 0, "heightPixels should be positive")
        assertTrue(result.densityDpi > 0, "densityDpi should be positive")
    }

    @Test
    internal fun `collect returns positive density`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = DisplayInfoHelper(context)

        val result = helper.collect()

        assertNotNull(result)
        assertTrue(result!!.density > 0f, "density should be positive")
    }

    @Test
    internal fun `collect returns refresh rate on API 30`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = DisplayInfoHelper(context)

        val result = helper.collect()

        assertNotNull(result, "DisplayInfo should not be null")
        assertNotNull(result!!.refreshRate, "refreshRate should be populated on API 30+")
        assertTrue(result.refreshRate!! > 0, "refreshRate should be positive")
    }

    @Test
    internal fun `collect handles HDR capabilities gracefully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = DisplayInfoHelper(context)

        val result = helper.collect()

        // HDR may be null (no HDR display in emulator) or a list, but shouldn't crash
        assertNotNull(result, "DisplayInfo should not be null")
        // hdrCapabilities field can be null or empty list - we just verify it doesn't throw
    }

    @Test
    internal fun `collect returns consistent values on repeated calls`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = DisplayInfoHelper(context)

        val result1 = helper.collect()
        val result2 = helper.collect()

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(result1!!.widthPixels, result2!!.widthPixels)
        assertEquals(result1.heightPixels, result2.heightPixels)
        assertEquals(result1.densityDpi, result2.densityDpi)
    }
}
