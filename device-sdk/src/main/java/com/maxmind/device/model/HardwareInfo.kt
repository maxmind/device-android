package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hardware resource information.
 */
@Serializable
public data class HardwareInfo(
    @SerialName("cpu_cores")
    val cpuCores: Int,
    @SerialName("total_memory_bytes")
    val totalMemoryBytes: Long,
    @SerialName("total_storage_bytes")
    val totalStorageBytes: Long,
)
