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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class DeviceIDsCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var collector: DeviceIDsCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)
        every { mockContext.contentResolver } returns mockContentResolver
        collector = DeviceIDsCollector(mockContext)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkStatic(Settings.Secure::class)
    }

    @Test
    internal fun `collect returns DeviceIDs with androidID when available`() {
        mockkStatic(Settings.Secure::class)
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ANDROID_ID)
        } returns "test-android-id"

        val result = collector.collect()

        assertNotNull(result)
        assertEquals("test-android-id", result.androidID)
    }

    @Test
    internal fun `collect returns null androidID when Settings throws exception`() {
        mockkStatic(Settings.Secure::class)
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ANDROID_ID)
        } throws SecurityException("Permission denied")

        val result = collector.collect()

        assertNotNull(result)
        assertNull(result.androidID)
    }

    @Test
    internal fun `collect returns null androidID when Settings returns null`() {
        mockkStatic(Settings.Secure::class)
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ANDROID_ID)
        } returns null

        val result = collector.collect()

        assertNotNull(result)
        assertNull(result.androidID)
    }

    @Test
    internal fun `WIDEVINE_UUID has correct value`() {
        // Widevine UUID is a well-known constant: EDEF8BA9-79D6-4ACE-A3C8-27DCD51D21ED
        val expectedUuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        assertEquals(expectedUuid, DeviceIDsCollector.WIDEVINE_UUID)
    }

    @Test
    internal fun `collect returns DeviceIDs object even when all values null`() {
        mockkStatic(Settings.Secure::class)
        every {
            Settings.Secure.getString(mockContentResolver, Settings.Secure.ANDROID_ID)
        } returns null

        val result = collector.collect()

        assertNotNull(result)
        // MediaDRM will be null in unit tests (no hardware)
        assertNull(result.mediaDrmID)
        assertNull(result.androidID)
    }
}
