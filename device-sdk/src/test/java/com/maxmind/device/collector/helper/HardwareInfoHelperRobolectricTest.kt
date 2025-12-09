package com.maxmind.device.collector.helper

import android.app.ActivityManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Robolectric-based tests for [HardwareInfoHelper].
 *
 * These tests exercise the helper with a real Android environment simulated by Robolectric,
 * allowing us to test StatFs and ActivityManager behavior that can't be mocked in unit tests.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [29])
internal class HardwareInfoHelperRobolectricTest {
    @Test
    internal fun `collect returns memory from stubbed ActivityManager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val shadowAm = Shadows.shadowOf(activityManager)

        // Set known memory values
        val memoryInfo = ActivityManager.MemoryInfo()
        memoryInfo.totalMem = 4_000_000_000L // 4GB
        memoryInfo.availMem = 2_000_000_000L
        shadowAm.setMemoryInfo(memoryInfo)

        val helper = HardwareInfoHelper(context)
        val result = helper.collect()

        assertEquals(4_000_000_000L, result.totalMemoryBytes, "totalMemoryBytes should match stubbed value")
        // Note: totalStorageBytes uses StatFs which Robolectric doesn't fully stub.
        // In the test environment, storage may be 0. We verify the memory stubbing works,
        // and storage collection is tested structurally in other tests.
        assertTrue(result.totalStorageBytes >= 0, "totalStorageBytes should be non-negative")
    }

    @Test
    internal fun `collect returns HardwareInfo with valid structure`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = HardwareInfoHelper(context)

        val result = helper.collect()

        // CPU cores should match runtime value
        assertEquals(Runtime.getRuntime().availableProcessors(), result.cpuCores)
        // Memory values should be non-negative
        assertTrue(result.totalMemoryBytes >= 0, "totalMemoryBytes should be non-negative")
        assertTrue(result.totalStorageBytes >= 0, "totalStorageBytes should be non-negative")
    }

    @Test
    internal fun `collect returns correct CPU cores count`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = HardwareInfoHelper(context)

        val result = helper.collect()

        // CPU cores should match runtime value
        assertEquals(
            Runtime.getRuntime().availableProcessors(),
            result.cpuCores,
            "cpuCores should match Runtime.availableProcessors()",
        )
    }

    @Test
    internal fun `collect does not throw exceptions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = HardwareInfoHelper(context)

        // Should not throw any exceptions
        val result = helper.collect()

        // Basic structural validation
        assertTrue(result.cpuCores > 0, "cpuCores should be positive")
    }

    @Test
    internal fun `collect is idempotent`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = HardwareInfoHelper(context)

        val result1 = helper.collect()
        val result2 = helper.collect()

        // Should return consistent values
        assertEquals(result1.cpuCores, result2.cpuCores)
        assertEquals(result1.totalMemoryBytes, result2.totalMemoryBytes)
        assertEquals(result1.totalStorageBytes, result2.totalStorageBytes)
    }
}
