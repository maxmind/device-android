package com.maxmind.device.collector

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SettingsCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var collector: SettingsCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        mockkStatic(Settings.System::class)
        mockkStatic(Settings.Global::class)

        collector = SettingsCollector(mockContext)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkStatic(Settings.System::class)
        unmockkStatic(Settings.Global::class)
    }

    @Test
    internal fun `collect returns settings with screen timeout`() {
        every {
            Settings.System.getInt(mockContentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } returns 60000

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(60000, result.screenTimeout)
    }

    @Test
    internal fun `collect returns settings with development settings enabled`() {
        every {
            Settings.Global.getInt(
                mockContentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            )
        } returns 1

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(true, result.developmentSettingsEnabled)
    }

    @Test
    internal fun `collect returns settings with development settings disabled`() {
        every {
            Settings.Global.getInt(
                mockContentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            )
        } returns 0

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(false, result.developmentSettingsEnabled)
    }

    @Test
    internal fun `collect returns settings with adb enabled`() {
        every {
            Settings.Global.getInt(mockContentResolver, Settings.Global.ADB_ENABLED)
        } returns 1

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(true, result.adbEnabled)
    }

    @Test
    internal fun `collect returns settings with animator duration scale`() {
        every {
            Settings.Global.getFloat(
                mockContentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
            )
        } returns 1.5f

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(1.5f, result.animatorDurationScale)
    }

    @Test
    internal fun `collect returns non-null SystemSettings even when settings unavailable`() {
        // Default mocks return 0 or throw, collector handles gracefully
        val result = collector.collect()

        assertNotNull(result)
    }
}
