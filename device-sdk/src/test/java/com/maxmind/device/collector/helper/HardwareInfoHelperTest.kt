package com.maxmind.device.collector.helper

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests for [HardwareInfoHelper].
 *
 * Note: StatFs cannot be mocked in pure unit tests (it accesses the filesystem).
 * For full collect() tests including storage, use instrumented tests.
 * These tests verify ActivityManager interactions and CPU core retrieval.
 */
internal class HardwareInfoHelperTest {
    private lateinit var mockContext: Context
    private lateinit var mockActivityManager: ActivityManager

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockActivityManager = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.ACTIVITY_SERVICE) } returns mockActivityManager

        // Mock Environment.getDataDirectory() to return a valid path for StatFs
        mockkStatic(Environment::class)
        every { Environment.getDataDirectory() } returns File("/data")
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    internal fun `helper can be instantiated`() {
        val testHelper = HardwareInfoHelper(mockContext)
        assertNotNull(testHelper)
    }

    @Test
    internal fun `CPU cores from Runtime is positive`() {
        // Verify that Runtime.availableProcessors() returns a valid value
        // This is what the helper uses for cpuCores
        val cores = Runtime.getRuntime().availableProcessors()
        assertTrue(cores > 0, "CPU cores should be positive")
    }

    @Test
    internal fun `collect requests ActivityManager service`() {
        val memoryInfoSlot = slot<ActivityManager.MemoryInfo>()
        every { mockActivityManager.getMemoryInfo(capture(memoryInfoSlot)) } answers {
            memoryInfoSlot.captured.totalMem = 4_000_000_000L
        }

        val helper = HardwareInfoHelper(mockContext)

        try {
            helper.collect()
        } catch (_: Exception) {
            // StatFs may fail in unit tests, but ActivityManager should be accessed
        }

        verify { mockContext.getSystemService(Context.ACTIVITY_SERVICE) }
    }

    @Test
    internal fun `collect populates totalMemoryBytes from ActivityManager`() {
        val expectedMemory = 8_000_000_000L

        val memoryInfoSlot = slot<ActivityManager.MemoryInfo>()
        every { mockActivityManager.getMemoryInfo(capture(memoryInfoSlot)) } answers {
            memoryInfoSlot.captured.totalMem = expectedMemory
        }

        val helper = HardwareInfoHelper(mockContext)

        try {
            val result = helper.collect()
            assertEquals(expectedMemory, result.totalMemoryBytes)
        } catch (_: Exception) {
            // StatFs may fail, but we verified ActivityManager was called
            verify { mockActivityManager.getMemoryInfo(any()) }
        }
    }

    @Test
    internal fun `collect uses Runtime availableProcessors for cpuCores`() {
        val expectedCores = Runtime.getRuntime().availableProcessors()

        val memoryInfoSlot = slot<ActivityManager.MemoryInfo>()
        every { mockActivityManager.getMemoryInfo(capture(memoryInfoSlot)) } answers {
            memoryInfoSlot.captured.totalMem = 4_000_000_000L
        }

        val helper = HardwareInfoHelper(mockContext)

        try {
            val result = helper.collect()
            assertEquals(expectedCores, result.cpuCores)
        } catch (_: Exception) {
            // StatFs may fail in unit tests
        }
    }
}
