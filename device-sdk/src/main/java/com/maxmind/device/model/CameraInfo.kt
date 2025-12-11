package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Camera hardware capabilities from CameraManager.
 */
@Serializable
public data class CameraInfo(
    @SerialName("camera_id")
    val cameraID: String,
    val facing: Int,
    @SerialName("sensor_physical_size")
    val sensorPhysicalSize: String? = null,
    @SerialName("supported_resolutions")
    val supportedResolutions: List<String> = emptyList(),
    @SerialName("focal_lengths")
    val focalLengths: List<Float>? = null,
)
