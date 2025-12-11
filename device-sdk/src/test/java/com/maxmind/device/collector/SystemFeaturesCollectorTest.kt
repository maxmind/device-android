package com.maxmind.device.collector

import android.content.Context
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SystemFeaturesCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager
    private lateinit var collector: SystemFeaturesCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockPackageManager = mockk(relaxed = true)
        every { mockContext.packageManager } returns mockPackageManager
        collector = SystemFeaturesCollector(mockContext)
    }

    @Test
    internal fun `collect returns sorted list of feature names`() {
        val features =
            arrayOf(
                createFeatureInfo("android.hardware.wifi"),
                createFeatureInfo("android.hardware.bluetooth"),
                createFeatureInfo("android.hardware.camera"),
            )
        every { mockPackageManager.systemAvailableFeatures } returns features

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(3, result.size)
        // Should be sorted alphabetically
        assertEquals("android.hardware.bluetooth", result[0])
        assertEquals("android.hardware.camera", result[1])
        assertEquals("android.hardware.wifi", result[2])
    }

    @Test
    internal fun `collect filters out null feature names`() {
        val features =
            arrayOf(
                createFeatureInfo("android.hardware.wifi"),
                createFeatureInfo(null),
                createFeatureInfo("android.hardware.camera"),
            )
        every { mockPackageManager.systemAvailableFeatures } returns features

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(2, result.size)
        assertTrue(result.contains("android.hardware.wifi"))
        assertTrue(result.contains("android.hardware.camera"))
    }

    @Test
    internal fun `collect returns empty list when no features available`() {
        every { mockPackageManager.systemAvailableFeatures } returns arrayOf()

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    internal fun `collect handles exception gracefully`() {
        every { mockPackageManager.systemAvailableFeatures } throws RuntimeException("Test error")

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    private fun createFeatureInfo(name: String?): FeatureInfo =
        FeatureInfo().apply {
            this.name = name
        }
}
