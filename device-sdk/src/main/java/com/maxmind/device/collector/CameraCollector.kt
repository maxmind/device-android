package com.maxmind.device.collector

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size
import com.maxmind.device.model.CameraInfo

/**
 * Collects camera hardware capabilities.
 *
 * Uses CameraManager to enumerate cameras and their characteristics
 * without requiring camera permission.
 */
internal class CameraCollector(
    private val context: Context,
    private val enableLogging: Boolean = false,
) {
    private companion object {
        private const val TAG = "CameraCollector"
    }

    /**
     * Collects information about all cameras on the device.
     *
     * @return List of [CameraInfo] for each available camera
     */
    fun collect(): List<CameraInfo> {
        val cameraManager =
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return emptyList()

        return try {
            cameraManager.cameraIdList.mapNotNull { cameraID ->
                collectCameraInfo(cameraManager, cameraID)
            }
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // CameraManager may throw on some devices
            if (enableLogging) {
                Log.d(TAG, "Failed to collect camera list: ${e.message}")
            }
            emptyList()
        }
    }

    private fun collectCameraInfo(
        cameraManager: CameraManager,
        cameraID: String,
    ): CameraInfo? =
        try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraID)

            val facing =
                characteristics.get(CameraCharacteristics.LENS_FACING)
                    ?: CameraCharacteristics.LENS_FACING_EXTERNAL

            val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
            val physicalSizeString = physicalSize?.let { "${it.width}x${it.height}" }

            val streamConfigMap =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val resolutions =
                streamConfigMap
                    ?.getOutputSizes(ImageFormat.JPEG)
                    ?.map { size: Size -> "${size.width}x${size.height}" }
                    ?: emptyList()

            val focalLengths =
                characteristics
                    .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.toList()

            CameraInfo(
                cameraID = cameraID,
                facing = facing,
                sensorPhysicalSize = physicalSizeString,
                supportedResolutions = resolutions,
                focalLengths = focalLengths,
            )
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // Individual camera info may fail, skip it
            if (enableLogging) {
                Log.d(TAG, "Failed to collect camera info for $cameraID: ${e.message}")
            }
            null
        }
}
