package com.maxmind.device.collector

import android.content.Context
import com.maxmind.device.collector.helper.BuildInfoHelper
import com.maxmind.device.collector.helper.DisplayInfoHelper
import com.maxmind.device.collector.helper.HardwareInfoHelper
import com.maxmind.device.collector.helper.InstallationInfoHelper
import com.maxmind.device.collector.helper.LocaleInfoHelper
import com.maxmind.device.model.BuildInfo
import com.maxmind.device.model.DisplayInfo
import com.maxmind.device.model.HardwareInfo
import com.maxmind.device.model.InstallationInfo
import com.maxmind.device.model.LocaleInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [DeviceDataCollector].
 *
 * These tests verify the helper injection and instantiation.
 * Full integration tests require instrumented tests due to
 * Android framework dependencies in dedicated collectors (GPU, Camera, etc.).
 */
internal class DeviceDataCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockBuildInfoHelper: BuildInfoHelper
    private lateinit var mockDisplayInfoHelper: DisplayInfoHelper
    private lateinit var mockHardwareInfoHelper: HardwareInfoHelper
    private lateinit var mockInstallationInfoHelper: InstallationInfoHelper
    private lateinit var mockLocaleInfoHelper: LocaleInfoHelper

    private val testBuildInfo =
        BuildInfo(
            fingerprint = "test-fingerprint",
            manufacturer = "Test",
            model = "TestModel",
            brand = "TestBrand",
            device = "testdevice",
            product = "testproduct",
            board = "testboard",
            hardware = "testhardware",
            osVersion = "14",
            sdkVersion = 34,
        )

    private val testDisplayInfo =
        DisplayInfo(
            widthPixels = 1080,
            heightPixels = 2400,
            densityDpi = 440,
            density = 2.75f,
        )

    private val testHardwareInfo =
        HardwareInfo(
            cpuCores = 8,
            totalMemoryBytes = 8_000_000_000L,
            totalStorageBytes = 128_000_000_000L,
        )

    private val testInstallationInfo =
        InstallationInfo(
            firstInstallTime = 1700000000000L,
            lastUpdateTime = 1700100000000L,
            versionCode = 10,
        )

    private val testLocaleInfo =
        LocaleInfo(
            language = "en",
            country = "US",
            timezone = "America/New_York",
        )

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockBuildInfoHelper = mockk(relaxed = true)
        mockDisplayInfoHelper = mockk(relaxed = true)
        mockHardwareInfoHelper = mockk(relaxed = true)
        mockInstallationInfoHelper = mockk(relaxed = true)
        mockLocaleInfoHelper = mockk(relaxed = true)

        // Setup default returns
        every { mockBuildInfoHelper.collect() } returns testBuildInfo
        every { mockDisplayInfoHelper.collect() } returns testDisplayInfo
        every { mockHardwareInfoHelper.collect() } returns testHardwareInfo
        every { mockInstallationInfoHelper.collect() } returns testInstallationInfo
        every { mockLocaleInfoHelper.collect() } returns testLocaleInfo
    }

    @Test
    internal fun `collector can be instantiated with context`() {
        val collector = DeviceDataCollector(mockContext)
        assertNotNull(collector)
    }

    @Test
    internal fun `collector can be instantiated with custom helpers`() {
        val collector =
            DeviceDataCollector(
                context = mockContext,
                buildInfoHelper = mockBuildInfoHelper,
                displayInfoHelper = mockDisplayInfoHelper,
                hardwareInfoHelper = mockHardwareInfoHelper,
                installationInfoHelper = mockInstallationInfoHelper,
                localeInfoHelper = mockLocaleInfoHelper,
            )
        assertNotNull(collector)
    }

    @Test
    internal fun `collector accepts enableLogging parameter`() {
        val collector =
            DeviceDataCollector(
                context = mockContext,
                enableLogging = true,
            )
        assertNotNull(collector)
    }

    @Test
    internal fun `helpers are called during instantiation verification`() {
        // Just verify the helpers can be set up - they don't get called until collect()
        val collector =
            DeviceDataCollector(
                context = mockContext,
                buildInfoHelper = mockBuildInfoHelper,
                displayInfoHelper = mockDisplayInfoHelper,
                hardwareInfoHelper = mockHardwareInfoHelper,
                installationInfoHelper = mockInstallationInfoHelper,
                localeInfoHelper = mockLocaleInfoHelper,
            )

        // Verify helpers were injected (not yet called)
        assertNotNull(collector)
        verify(exactly = 0) { mockBuildInfoHelper.collect() }
        verify(exactly = 0) { mockDisplayInfoHelper.collect() }
    }
}
