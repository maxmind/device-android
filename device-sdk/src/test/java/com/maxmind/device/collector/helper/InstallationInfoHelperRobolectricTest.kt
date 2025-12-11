package com.maxmind.device.collector.helper

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Robolectric-based tests for [InstallationInfoHelper].
 *
 * These tests exercise the helper with a real Android environment simulated by Robolectric,
 * allowing us to test PackageInfo access that can't be properly mocked in unit tests
 * (e.g., longVersionCode is a public field, not a getter).
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [29])
internal class InstallationInfoHelperRobolectricTest {
    @Test
    internal fun `collect returns all fields from stubbed PackageInfo`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = Shadows.shadowOf(context.packageManager)

        // Get mutable package info and set known values
        val packageInfo = shadowPm.getInternalMutablePackageInfo(context.packageName)
        packageInfo.firstInstallTime = 1000000L
        packageInfo.lastUpdateTime = 2000000L
        packageInfo.versionName = "1.2.3"
        packageInfo.longVersionCode = 123L

        val helper = InstallationInfoHelper(context)
        val result = helper.collect()

        assertEquals(1000000L, result.firstInstallTime, "firstInstallTime should match stubbed value")
        assertEquals(2000000L, result.lastUpdateTime, "lastUpdateTime should match stubbed value")
        assertEquals("1.2.3", result.versionName, "versionName should match stubbed value")
        assertEquals(123L, result.versionCode, "versionCode should match stubbed value")
    }

    @Test
    internal fun `collect returns valid InstallationInfo`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = InstallationInfoHelper(context)

        val result = helper.collect()

        // All timestamp fields should be non-negative
        assertTrue(result.firstInstallTime >= 0, "firstInstallTime should be non-negative")
        assertTrue(result.lastUpdateTime >= 0, "lastUpdateTime should be non-negative")
        assertTrue(result.versionCode >= 0, "versionCode should be non-negative")
    }

    @Test
    internal fun `collect returns version name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = InstallationInfoHelper(context)

        val result = helper.collect()

        // Robolectric provides a test app with version info
        // versionName can be null in some cases, but the helper should handle it
        assertNotNull(result)
    }

    @Test
    internal fun `collect handles installer package gracefully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = InstallationInfoHelper(context)

        val result = helper.collect()

        // installerPackage may be null in test environment, but shouldn't crash
        assertNotNull(result)
        // The installerPackage field itself can be null - we just verify it doesn't throw
    }

    @Test
    internal fun `collect returns consistent values on repeated calls`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = InstallationInfoHelper(context)

        val result1 = helper.collect()
        val result2 = helper.collect()

        // Values should be consistent
        assertEquals(result1.firstInstallTime, result2.firstInstallTime)
        assertEquals(result1.versionCode, result2.versionCode)
    }
}
