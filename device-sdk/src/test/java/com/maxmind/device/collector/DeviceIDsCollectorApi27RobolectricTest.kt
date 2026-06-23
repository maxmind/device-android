package com.maxmind.device.collector

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension

/**
 * Best-effort Robolectric smoke test for [DeviceIDsCollector] on API 27 (Android 8.1).
 *
 * On API < 28 the collector releases [android.media.MediaDrm] via the deprecated
 * `release()` lifecycle call instead of `close()` (which only exists from API 28).
 * This test pins the SDK to 27 and asserts that [DeviceIDsCollector.collect] returns a
 * [com.maxmind.device.model.DeviceIDs] without throwing.
 *
 * Known limitation: Robolectric's MediaDrm shadow does not exercise the real
 * `release()`/`close()` split, so this test cannot fully validate the cleanup branch —
 * consistent with how [DeviceIDsCollectorTest] treats MediaDRM as null in unit tests.
 * The real branch is validated by lint's NewApi check and (optionally) an instrumented
 * test on an API-27 device.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [27])
internal class DeviceIDsCollectorApi27RobolectricTest {
    @Test
    internal fun `collect returns DeviceIDs without throwing on API 27`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val collector = DeviceIDsCollector(context)

        val result = collector.collect()

        assertNotNull(result)
    }
}
