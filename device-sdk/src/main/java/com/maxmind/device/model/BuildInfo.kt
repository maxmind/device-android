package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device build information from android.os.Build.
 */
@Serializable
public data class BuildInfo(
    val fingerprint: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val bootloader: String? = null,
    @SerialName("os_version")
    val osVersion: String,
    @SerialName("sdk_version")
    val sdkVersion: Int,
    @SerialName("security_patch")
    val securityPatch: String? = null,
    @SerialName("supported_abis")
    val supportedAbis: List<String> = emptyList(),
)
