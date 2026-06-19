package com.maxmind.device.collector.helper

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Robolectric-based tests for [InstallationInfoHelper] on API 27 (Android 8.1).
 *
 * On API < 28 the helper falls back to the deprecated int [android.content.pm.PackageInfo.versionCode]
 * field (read as `.versionCode.toLong()`) and to [android.content.pm.PackageManager.getInstallerPackageName]
 * for the installer. JUnit 5's Robolectric extension can only set `@Config(sdk)` at the class
 * level, so this separate class pins the SDK to 27 to exercise those legacy branches; the
 * API-28+ branches stay covered by [InstallationInfoHelperRobolectricTest] at sdk 29.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [27])
internal class InstallationInfoHelperApi27RobolectricTest {
    @Test
    internal fun `collect reads versionCode from legacy int field on API 27`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = Shadows.shadowOf(context.packageManager)

        // On API 27 the helper reads the deprecated int versionCode field, not longVersionCode.
        val packageInfo = shadowPm.getInternalMutablePackageInfo(context.packageName)
        @Suppress("DEPRECATION")
        packageInfo.versionCode = 456
        packageInfo.versionName = "9.8.7"

        val helper = InstallationInfoHelper(context)
        val result = helper.collect()

        assertEquals(456L, result.versionCode, "versionCode should come from the legacy int field")
        assertEquals("9.8.7", result.versionName, "versionName should match stubbed value")
    }

    @Test
    internal fun `collect resolves installer via legacy branch without throwing on API 27`() {
        // On API < 30 the helper takes the getInstallerPackageName branch. The installer is
        // typically null in the Robolectric environment; we only verify the legacy branch is
        // exercised without throwing (mirroring the sdk-29 InstallationInfoHelperRobolectricTest).
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = InstallationInfoHelper(context)

        val result = helper.collect()

        assertNotNull(result)
    }
}
