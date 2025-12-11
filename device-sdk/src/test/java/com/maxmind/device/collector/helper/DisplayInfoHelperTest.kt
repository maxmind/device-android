package com.maxmind.device.collector.helper

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DisplayInfoHelperTest {
    private lateinit var mockContext: Context
    private lateinit var mockDisplayManager: DisplayManager
    private lateinit var mockDisplay: Display
    private lateinit var helper: DisplayInfoHelper

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockDisplayManager = mockk(relaxed = true)
        mockDisplay = mockk(relaxed = true)

        every { mockContext.getSystemService(Context.DISPLAY_SERVICE) } returns mockDisplayManager
        every { mockDisplayManager.getDisplay(Display.DEFAULT_DISPLAY) } returns mockDisplay
        every { mockDisplay.refreshRate } returns 60.0f

        helper = DisplayInfoHelper(mockContext)
    }

    @Test
    internal fun `collect returns DisplayInfo when display available`() {
        val metricsSlot = slot<DisplayMetrics>()
        every { mockDisplay.getMetrics(capture(metricsSlot)) } answers {
            metricsSlot.captured.widthPixels = 1080
            metricsSlot.captured.heightPixels = 1920
            metricsSlot.captured.densityDpi = 420
            metricsSlot.captured.density = 2.625f
        }

        val result = helper.collect()

        assertNotNull(result)
    }

    @Test
    internal fun `collect returns correct display dimensions`() {
        val metricsSlot = slot<DisplayMetrics>()
        every { mockDisplay.getMetrics(capture(metricsSlot)) } answers {
            metricsSlot.captured.widthPixels = 1440
            metricsSlot.captured.heightPixels = 2560
            metricsSlot.captured.densityDpi = 560
            metricsSlot.captured.density = 3.5f
        }

        val result = helper.collect()

        assertNotNull(result)
        assertEquals(1440, result!!.widthPixels)
        assertEquals(2560, result.heightPixels)
    }

    @Test
    internal fun `collect returns correct density values`() {
        val metricsSlot = slot<DisplayMetrics>()
        every { mockDisplay.getMetrics(capture(metricsSlot)) } answers {
            metricsSlot.captured.widthPixels = 1080
            metricsSlot.captured.heightPixels = 1920
            metricsSlot.captured.densityDpi = 480
            metricsSlot.captured.density = 3.0f
        }

        val result = helper.collect()

        assertNotNull(result)
        assertEquals(480, result!!.densityDpi)
        assertEquals(3.0f, result.density)
    }

    @Test
    internal fun `collect returns refresh rate from Display`() {
        val metricsSlot = slot<DisplayMetrics>()
        every { mockDisplay.getMetrics(capture(metricsSlot)) } answers {
            metricsSlot.captured.widthPixels = 1080
            metricsSlot.captured.heightPixels = 1920
            metricsSlot.captured.densityDpi = 420
            metricsSlot.captured.density = 2.625f
        }
        every { mockDisplay.refreshRate } returns 120.0f

        val result = helper.collect()

        assertNotNull(result)
        assertEquals(120.0f, result!!.refreshRate)
    }

    @Test
    internal fun `collect returns 90Hz refresh rate`() {
        val metricsSlot = slot<DisplayMetrics>()
        every { mockDisplay.getMetrics(capture(metricsSlot)) } answers {
            metricsSlot.captured.widthPixels = 1080
            metricsSlot.captured.heightPixels = 2400
            metricsSlot.captured.densityDpi = 400
            metricsSlot.captured.density = 2.5f
        }
        every { mockDisplay.refreshRate } returns 90.0f

        val result = helper.collect()

        assertNotNull(result)
        assertEquals(90.0f, result!!.refreshRate)
    }

    /**
     * HDR capabilities tests require API level 24+ (Build.VERSION.SDK_INT >= N).
     * In unit tests, Build.VERSION.SDK_INT is 0, so hdrCapabilities will always be null.
     * Full HDR testing requires instrumented tests on API 24+ devices.
     */
    @Test
    internal fun `collect returns null hdrCapabilities in unit tests due to API level`() {
        val metricsSlot = slot<DisplayMetrics>()
        every { mockDisplay.getMetrics(capture(metricsSlot)) } answers {
            metricsSlot.captured.widthPixels = 1080
            metricsSlot.captured.heightPixels = 1920
            metricsSlot.captured.densityDpi = 420
            metricsSlot.captured.density = 2.625f
        }

        // Mock HDR capabilities - but these won't be accessed in unit tests
        // because Build.VERSION.SDK_INT < Build.VERSION_CODES.N
        val mockHdrCapabilities =
            mockk<Display.HdrCapabilities> {
                every { supportedHdrTypes } returns intArrayOf(2, 3) // HDR10, HLG
            }
        every { mockDisplay.hdrCapabilities } returns mockHdrCapabilities

        val result = helper.collect()

        assertNotNull(result)
        // In unit tests, Build.VERSION.SDK_INT is 0, so HDR is null
        assertNull(result!!.hdrCapabilities)
    }

    @Test
    internal fun `collect returns null when DisplayManager unavailable`() {
        every { mockContext.getSystemService(Context.DISPLAY_SERVICE) } returns null
        val helperWithNoDisplay = DisplayInfoHelper(mockContext)

        val result = helperWithNoDisplay.collect()

        assertNull(result)
    }

    @Test
    internal fun `collect returns null when default display unavailable`() {
        every { mockDisplayManager.getDisplay(Display.DEFAULT_DISPLAY) } returns null

        val result = helper.collect()

        assertNull(result)
    }
}
