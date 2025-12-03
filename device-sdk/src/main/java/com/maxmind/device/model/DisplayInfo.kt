package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Display characteristics from DisplayMetrics.
 */
@Serializable
public data class DisplayInfo(
    @SerialName("width_pixels")
    val widthPixels: Int,
    @SerialName("height_pixels")
    val heightPixels: Int,
    @SerialName("density_dpi")
    val densityDpi: Int,
    val density: Float,
    @SerialName("refresh_rate")
    val refreshRate: Float? = null,
    @SerialName("hdr_capabilities")
    val hdrCapabilities: List<Int>? = null,
)
