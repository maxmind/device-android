package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about a device sensor.
 */
@Serializable
public data class SensorInfo(
    val name: String,
    val vendor: String,
    val type: Int,
    val version: Int,
    @SerialName("max_range")
    val maxRange: Float,
    val resolution: Float,
    val power: Float,
)
