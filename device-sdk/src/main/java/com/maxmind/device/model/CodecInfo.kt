package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Media codec support information.
 */
@Serializable
internal data class CodecInfo(
    val audio: List<CodecDetail> = emptyList(),
    val video: List<CodecDetail> = emptyList(),
)

/**
 * Details about a specific codec.
 */
@Serializable
internal data class CodecDetail(
    val name: String,
    @SerialName("supported_types")
    val supportedTypes: List<String> = emptyList(),
    @SerialName("is_encoder")
    val isEncoder: Boolean,
)
