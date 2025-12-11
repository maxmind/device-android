package com.maxmind.device.collector

import android.hardware.camera2.CameraCharacteristics
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CameraCollector].
 *
 * These tests run on an Android device/emulator to verify camera info collection
 * using real Camera2 API.
 */
@RunWith(AndroidJUnit4::class)
public class CameraCollectorInstrumentedTest {
    @Test
    public fun collect_returnsCameraListOrEmpty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val collector = CameraCollector(context)

        val result = collector.collect()

        assertNotNull("Camera list should not be null", result)
        // Most devices have at least one camera, but emulators may not
    }

    @Test
    public fun collect_cameraInfoHasValidFacing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val collector = CameraCollector(context)

        val result = collector.collect()

        result.forEach { cameraInfo ->
            assertNotNull("Camera ID should not be null", cameraInfo.cameraID)
            assertTrue(
                "Facing should be FRONT (0), BACK (1), or EXTERNAL (2)",
                cameraInfo.facing in
                    listOf(
                        CameraCharacteristics.LENS_FACING_FRONT,
                        CameraCharacteristics.LENS_FACING_BACK,
                        CameraCharacteristics.LENS_FACING_EXTERNAL,
                    ),
            )
        }
    }

    @Test
    public fun collect_doesNotCrashOnRepeatedCalls() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val collector = CameraCollector(context)

        // Multiple calls should not crash
        repeat(3) {
            val result = collector.collect()
            assertNotNull(result)
        }
    }
}
