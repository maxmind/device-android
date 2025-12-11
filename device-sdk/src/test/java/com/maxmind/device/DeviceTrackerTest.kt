package com.maxmind.device

import android.content.Context
import com.maxmind.device.config.SdkConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

/**
 * Tests for [DeviceTracker] singleton behavior.
 *
 * These tests exercise the real singleton lifecycle in a controlled sequence.
 * Tests are ordered to form a complete lifecycle test:
 * 1. Verify uninitialized state
 * 2. Verify getInstance throws when uninitialized
 * 3. Initialize and verify
 * 4. Verify getInstance returns same instance
 * 5. Verify double-init throws
 * 6. Verify shutdown works
 *
 * Note: The singleton cannot be reliably reset between tests due to
 * Kotlin @Volatile semantics and JVM security restrictions. Therefore,
 * tests are ordered to exercise the natural lifecycle.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DeviceTrackerTest {
    private lateinit var mockContext: Context
    private lateinit var mockApplicationContext: Context
    private lateinit var config: SdkConfig
    private var initializedTracker: DeviceTracker? = null

    @BeforeAll
    internal fun setUp() {
        // Reset singleton at start of test class (best effort)
        resetSingleton()

        mockApplicationContext = mockk(relaxed = true)
        mockContext =
            mockk(relaxed = true) {
                every { applicationContext } returns mockApplicationContext
            }
        config = SdkConfig.Builder(12345).build()
    }

    @Test
    @Order(1)
    internal fun `01 isInitialized returns false before initialize`() {
        // Verify real singleton state - should be false initially
        assertFalse(DeviceTracker.isInitialized(), "SDK should not be initialized at start")
    }

    @Test
    @Order(2)
    internal fun `02 getInstance throws before initialize`() {
        // Verify real exception is thrown when not initialized
        val exception =
            assertThrows(IllegalStateException::class.java) {
                DeviceTracker.getInstance()
            }

        assertEquals("SDK not initialized. Call initialize() first.", exception.message)
    }

    @Test
    @Order(3)
    internal fun `03 initialize creates instance and sets initialized state`() {
        assertFalse(DeviceTracker.isInitialized(), "Precondition: SDK should not be initialized")

        initializedTracker = DeviceTracker.initialize(mockContext, config)

        assertNotNull(initializedTracker)
        assertTrue(DeviceTracker.isInitialized(), "SDK should be initialized after initialize()")
    }

    @Test
    @Order(4)
    internal fun `04 getInstance returns same instance after initialize`() {
        assertTrue(DeviceTracker.isInitialized(), "Precondition: SDK should be initialized")

        val retrieved = DeviceTracker.getInstance()

        assertSame(initializedTracker, retrieved, "getInstance should return the same instance")
    }

    @Test
    @Order(5)
    internal fun `05 initialize throws if already initialized`() {
        assertTrue(DeviceTracker.isInitialized(), "Precondition: SDK should be initialized")

        // Second initialization should throw
        val exception =
            assertThrows(IllegalStateException::class.java) {
                DeviceTracker.initialize(mockContext, config)
            }

        assertEquals("SDK is already initialized", exception.message)
    }

    @Test
    @Order(6)
    internal fun `06 shutdown can be called after initialize`() {
        assertTrue(DeviceTracker.isInitialized(), "Precondition: SDK should be initialized")

        val tracker = DeviceTracker.getInstance()

        // Should not throw
        tracker.shutdown()

        // Note: shutdown doesn't reset the singleton instance,
        // it just cancels coroutines and closes the HTTP client
    }

    @Test
    @Order(7)
    internal fun `07 config builder creates valid config`() {
        // This test doesn't depend on singleton state
        val testConfig =
            SdkConfig
                .Builder(67890)
                .enableLogging(true)
                .collectionInterval(30000)
                .build()

        assertEquals(67890, testConfig.accountID)
        assertTrue(testConfig.enableLogging)
        assertEquals(30000L, testConfig.collectionIntervalMs)
    }

    @Test
    @Order(8)
    internal fun `08 config builder validates accountID`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                SdkConfig.Builder(0).build()
            }

        assertEquals("Account ID must be positive", exception.message)
    }

    @Test
    @Order(9)
    internal fun `09 config builder validates negative accountID`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                SdkConfig.Builder(-1).build()
            }

        assertEquals("Account ID must be positive", exception.message)
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
