package com.maxmind.device.collector

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [GpuCollector].
 *
 * These tests run on an Android device/emulator to verify GPU info collection
 * using real EGL contexts.
 */
@RunWith(AndroidJUnit4::class)
public class GpuCollectorInstrumentedTest {
    @Test
    public fun collect_returnsGpuInfoOrNullGracefully() {
        val collector = GpuCollector()

        val result = collector.collect()

        // On most devices, GPU info should be available
        // On emulators without GPU support, null is acceptable
        if (result != null) {
            // If we got a result, it should have meaningful data
            assertTrue(
                "Renderer or vendor should be non-null",
                result.renderer != null || result.vendor != null,
            )
        }
        // Test passes whether result is null or non-null
        // The key is that it doesn't crash
    }

    @Test
    public fun collect_doesNotCrashOnRepeatedCalls() {
        val collector = GpuCollector()

        // Multiple calls should not leak EGL resources or crash
        repeat(5) {
            val result = collector.collect()
            // Just verify no crash - result can be null or non-null
        }
    }

    @Test
    public fun collect_handlesMultipleCollectorInstances() {
        // Verify that multiple collector instances don't interfere
        val collector1 = GpuCollector()
        val collector2 = GpuCollector()

        val result1 = collector1.collect()
        val result2 = collector2.collect()

        // Results should be consistent (both null or both non-null with same values)
        if (result1 != null && result2 != null) {
            assertTrue(
                "Renderer should be consistent across instances",
                result1.renderer == result2.renderer,
            )
        }
    }
}
