package com.maxmind.device

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.maxmind.device.config.SdkConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Robolectric-based tests for [DeviceTracker] singleton lifecycle.
 *
 * These tests exercise the real singleton with a full Android environment,
 * providing more reliable singleton reset via Robolectric's test isolation
 * compared to pure unit tests.
 *
 * The resetSingleton() method now fails fast with AssertionError if reset
 * cannot be performed, ensuring tests don't run with stale singleton state.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [29])
internal class DeviceTrackerRobolectricTest {
    @BeforeEach
    internal fun setUp() {
        resetSingleton()
    }

    @AfterEach
    internal fun tearDown() {
        try {
            if (DeviceTracker.isInitialized()) {
                DeviceTracker.getInstance().shutdown()
            }
        } catch (_: Exception) {
            // Ignore errors during cleanup
        }
        try {
            resetSingleton()
        } catch (_: AssertionError) {
            // Ignore reset errors in teardown - next test's setup will catch it
        }
    }

    @Test
    internal fun `isInitialized returns false before initialize`() {
        assertFalse(DeviceTracker.isInitialized(), "SDK should not be initialized before initialize()")
    }

    @Test
    internal fun `initialize creates instance and sets initialized state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SdkConfig.Builder(12345).build()

        val tracker = DeviceTracker.initialize(context, config)

        assertNotNull(tracker, "initialize should return a non-null tracker")
        assertTrue(DeviceTracker.isInitialized(), "SDK should be initialized after initialize()")
    }

    @Test
    internal fun `getInstance returns same instance after initialize`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SdkConfig.Builder(12345).build()

        val initialized = DeviceTracker.initialize(context, config)
        val retrieved = DeviceTracker.getInstance()

        assertSame(initialized, retrieved, "getInstance should return the same instance from initialize")
    }

    @Test
    internal fun `initialize throws if already initialized`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SdkConfig.Builder(12345).build()

        DeviceTracker.initialize(context, config)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                DeviceTracker.initialize(context, config)
            }

        assertTrue(
            exception.message?.contains("already initialized") == true,
            "Exception message should mention already initialized",
        )
    }

    @Test
    internal fun `collectDeviceData returns valid data`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SdkConfig.Builder(12345).build()

        val tracker = DeviceTracker.initialize(context, config)
        val data = tracker.collectDeviceData()

        assertNotNull(data, "collectDeviceData should return non-null DeviceData")
        assertNotNull(data.build, "DeviceData.build should not be null")
        assertNotNull(data.display, "DeviceData.display should not be null")
        assertNotNull(data.hardware, "DeviceData.hardware should not be null")
    }

    @Test
    internal fun `collectDeviceData returns consistent data on repeated calls`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SdkConfig.Builder(12345).build()

        val tracker = DeviceTracker.initialize(context, config)

        val data1 = tracker.collectDeviceData()
        val data2 = tracker.collectDeviceData()

        // Static fields should be consistent
        assertEquals(data1.build.manufacturer, data2.build.manufacturer)
        assertEquals(data1.build.model, data2.build.model)
        assertEquals(data1.hardware.cpuCores, data2.hardware.cpuCores)
    }

    /**
     * Resets the singleton instance using reflection.
     *
     * @throws AssertionError if reset fails - tests should not continue with stale state
     */
    private fun resetSingleton() {
        try {
            // The 'instance' field is a static field on DeviceTracker class itself,
            // not on the companion object (Kotlin compiles companion val/var to static fields)
            val instanceField = DeviceTracker::class.java.getDeclaredField("instance")
            instanceField.isAccessible = true
            instanceField.set(null, null)

            // Verify reset worked
            check(!DeviceTracker.isInitialized()) { "Singleton reset failed - instance still exists" }
        } catch (e: Exception) {
            throw AssertionError("Cannot reset DeviceTracker singleton - tests invalid: ${e.message}", e)
        }
    }
}
