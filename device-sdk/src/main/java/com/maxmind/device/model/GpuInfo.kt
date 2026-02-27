package com.maxmind.device.model

import kotlinx.serialization.Serializable

/**
 * GPU information from OpenGL ES.
 */
@Serializable
internal data class GpuInfo(
    val renderer: String? = null,
    val vendor: String? = null,
    val version: String? = null,
    val extensions: List<String>? = null,
)
