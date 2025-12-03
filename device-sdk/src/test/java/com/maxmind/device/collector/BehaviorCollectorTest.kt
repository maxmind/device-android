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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BehaviorCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var collector: BehaviorCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver

        mockkStatic(Settings.Secure::class)

        collector = BehaviorCollector(mockContext)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkStatic(Settings.Secure::class)
    }

    @Test
    internal fun `collect returns enabled keyboards`() {
        val keyboards =
            "com.google.android.inputmethod.latin/com.android.inputmethod.latin" +
                ".LatinIME:com.swiftkey.swiftkey/com.touchtype.KeyboardService"
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
        } returns keyboards

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(2, result.enabledKeyboards.size)
        assertTrue(
            result.enabledKeyboards.contains(
                "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME",
            ),
        )
        assertTrue(
            result.enabledKeyboards.contains("com.swiftkey.swiftkey/com.touchtype.KeyboardService"),
        )
    }

    @Test
    internal fun `collect returns enabled accessibility services`() {
        every {
            Settings.Secure.getString(
                mockContentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            )
        } returns "com.example.accessibility/com.example.AccessibilityService"

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(1, result.enabledAccessibilityServices.size)
        assertEquals(
            "com.example.accessibility/com.example.AccessibilityService",
            result.enabledAccessibilityServices[0],
        )
    }

    @Test
    internal fun `collect returns empty lists when no services enabled`() {
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
        } returns null
        every {
            Settings.Secure.getString(
                mockContentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            )
        } returns null

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.enabledKeyboards.isEmpty())
        assertTrue(result.enabledAccessibilityServices.isEmpty())
    }

    @Test
    internal fun `collect filters out blank entries`() {
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
        } returns "com.example.keyboard:::"

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(1, result.enabledKeyboards.size)
        assertEquals("com.example.keyboard", result.enabledKeyboards[0])
    }

    @Test
    internal fun `collect handles exception gracefully`() {
        every {
            Settings.Secure.getString(mockContentResolver, any())
        } throws SecurityException("Permission denied")

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.enabledKeyboards.isEmpty())
        assertTrue(result.enabledAccessibilityServices.isEmpty())
    }
}
