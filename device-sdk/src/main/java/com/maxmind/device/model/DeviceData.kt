package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Comprehensive device information collected by the SDK.
 *
 * This data class contains all device attributes that can be collected
 * and sent to MaxMind servers for device fingerprinting and fraud detection.
 */
@Serializable
public data class DeviceData(
    // Server-generated stored ID (like browser cookies)
    @SerialName("stored_id")
    val storedID: StoredID = StoredID(),
    // Device-generated identifiers
    @SerialName("device_ids")
    val deviceIDs: DeviceIDs = DeviceIDs(),
    // Device info
    val build: BuildInfo,
    val display: DisplayInfo,
    val hardware: HardwareInfo,
    // Subsystems
    val gpu: GpuInfo? = null,
    val audio: AudioInfo? = null,
    val sensors: List<SensorInfo> = emptyList(),
    val cameras: List<CameraInfo> = emptyList(),
    val codecs: CodecInfo = CodecInfo(),
    // System state
    @SerialName("system_features")
    val systemFeatures: List<String> = emptyList(),
    val network: NetworkInfo? = null,
    val installation: InstallationInfo,
    val settings: SystemSettings = SystemSettings(),
    val behavior: BehaviorInfo = BehaviorInfo(),
    // Context
    val locale: LocaleInfo,
    @SerialName("timezone_offset")
    val timezoneOffset: Int,
    @SerialName("device_time")
    val deviceTime: Long = System.currentTimeMillis(),
    @SerialName("webview_user_agent")
    val webViewUserAgent: String? = null,
)
