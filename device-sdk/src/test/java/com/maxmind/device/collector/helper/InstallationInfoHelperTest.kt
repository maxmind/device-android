package com.maxmind.device.collector.helper

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [InstallationInfoHelper].
 *
 * Note: Success-path tests that call collect() require instrumented tests
 * because PackageInfo.longVersionCode accesses Android framework internals
 * that cannot be properly stubbed in pure unit tests.
 *
 * These tests verify:
 * - Helper instantiation
 * - Error handling (NameNotFoundException)
 * - Correct PackageManager access patterns
 */
internal class InstallationInfoHelperTest {
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager
    private lateinit var helper: InstallationInfoHelper

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockPackageManager = mockk(relaxed = true)
        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.packageName } returns "com.test.app"
        helper = InstallationInfoHelper(mockContext)
    }

    @Test
    internal fun `helper can be instantiated`() {
        val testHelper = InstallationInfoHelper(mockContext)
        assertNotNull(testHelper)
    }

    @Test
    internal fun `collect throws when package not found`() {
        every {
            mockPackageManager.getPackageInfo("com.test.app", 0)
        } throws PackageManager.NameNotFoundException("Package not found")

        assertThrows(PackageManager.NameNotFoundException::class.java) {
            helper.collect()
        }
    }

    @Test
    internal fun `collect accesses package manager with correct package name`() {
        every {
            mockPackageManager.getPackageInfo("com.test.app", 0)
        } throws PackageManager.NameNotFoundException("Expected for test")

        try {
            helper.collect()
        } catch (_: PackageManager.NameNotFoundException) {
            // Expected
        }

        verify { mockPackageManager.getPackageInfo("com.test.app", 0) }
    }

    @Test
    internal fun `helper uses context packageName for lookup`() {
        every { mockContext.packageName } returns "com.different.app"
        val customHelper = InstallationInfoHelper(mockContext)

        every {
            mockPackageManager.getPackageInfo("com.different.app", 0)
        } throws PackageManager.NameNotFoundException("Expected for test")

        try {
            customHelper.collect()
        } catch (_: PackageManager.NameNotFoundException) {
            // Expected
        }

        verify { mockPackageManager.getPackageInfo("com.different.app", 0) }
    }

    @Test
    internal fun `helper accesses packageManager during collect`() {
        every {
            mockPackageManager.getPackageInfo("com.test.app", 0)
        } throws PackageManager.NameNotFoundException("Expected for test")

        try {
            helper.collect()
        } catch (_: PackageManager.NameNotFoundException) {
            // Expected
        }

        // Verify packageManager was accessed
        verify { mockContext.packageManager }
    }
}
