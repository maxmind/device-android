package com.maxmind.device.collector

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Size
import android.util.SizeF
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Tests for CameraCollector.
 *
 * Uses MockK to mock CameraManager and CameraCharacteristics.
 */
internal class CameraCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockCameraManager: CameraManager
    private lateinit var collector: CameraCollector

    // CameraCharacteristics.LENS_FACING_* constants
    private val lensFacingBack = 1 // CameraCharacteristics.LENS_FACING_BACK
    private val lensFacingFront = 0 // CameraCharacteristics.LENS_FACING_FRONT

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockCameraManager = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.CAMERA_SERVICE) } returns mockCameraManager
        collector = CameraCollector(mockContext)
    }

    @Test
    @Disabled("CameraCharacteristics.LENS_FACING key is null in unit tests - use instrumented tests")
    internal fun `collect returns camera list with properties`() {
        val mockStreamMap =
            mockk<StreamConfigurationMap> {
                // ImageFormat.JPEG = 256
                every { getOutputSizes(ImageFormat.JPEG) } returns
                    arrayOf(
                        Size(4032, 3024),
                        Size(1920, 1080),
                    )
            }
        val mockCharacteristics =
            mockk<CameraCharacteristics> {
                every { get(CameraCharacteristics.LENS_FACING) } returns lensFacingBack
                every { get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) } returns
                    SizeF(
                        6.4f,
                        4.8f,
                    )
                every { get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) } returns
                    mockStreamMap
                every { get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS) } returns
                    floatArrayOf(4.25f, 6.0f)
            }
        every { mockCameraManager.cameraIdList } returns arrayOf("0")
        every { mockCameraManager.getCameraCharacteristics("0") } returns mockCharacteristics

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("0", result[0].cameraID)
        assertEquals(lensFacingBack, result[0].facing)
        assertEquals("6.4x4.8", result[0].sensorPhysicalSize)
        assertEquals(listOf("4032x3024", "1920x1080"), result[0].supportedResolutions)
        assertEquals(listOf(4.25f, 6.0f), result[0].focalLengths)
    }

    @Test
    @Disabled("CameraCharacteristics.LENS_FACING key is null in unit tests - use instrumented tests")
    internal fun `collect returns multiple cameras`() {
        val backCameraChars =
            mockk<CameraCharacteristics>(relaxed = true) {
                every { get(CameraCharacteristics.LENS_FACING) } returns lensFacingBack
            }
        val frontCameraChars =
            mockk<CameraCharacteristics>(relaxed = true) {
                every { get(CameraCharacteristics.LENS_FACING) } returns lensFacingFront
            }
        every { mockCameraManager.cameraIdList } returns arrayOf("0", "1")
        every { mockCameraManager.getCameraCharacteristics("0") } returns backCameraChars
        every { mockCameraManager.getCameraCharacteristics("1") } returns frontCameraChars

        val result = collector.collect()

        assertEquals(2, result.size)
        assertEquals(lensFacingBack, result[0].facing)
        assertEquals(lensFacingFront, result[1].facing)
    }

    @Test
    internal fun `collect returns empty list when no cameras available`() {
        every { mockCameraManager.cameraIdList } returns arrayOf()

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    internal fun `collect returns empty list when CameraManager unavailable`() {
        every { mockContext.getSystemService(Context.CAMERA_SERVICE) } returns null
        val collectorWithNoCameraManager = CameraCollector(mockContext)

        val result = collectorWithNoCameraManager.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    internal fun `collect handles exception from CameraManager gracefully`() {
        every { mockCameraManager.cameraIdList } throws SecurityException("Camera access denied")

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}
