package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Audio hardware profile from AudioManager.
 */
@Serializable
public data class AudioInfo(
    @SerialName("output_sample_rate")
    val outputSampleRate: String? = null,
    @SerialName("output_frames_per_buffer")
    val outputFramesPerBuffer: String? = null,
)
