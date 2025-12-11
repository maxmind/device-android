package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network context information.
 */
@Serializable
public data class NetworkInfo(
    @SerialName("connection_type")
    val connectionType: String? = null,
    @SerialName("is_metered")
    val isMetered: Boolean? = null,
    @SerialName("link_downstream_bandwidth_kbps")
    val linkDownstreamBandwidthKbps: Int? = null,
    @SerialName("wifi_frequency")
    val wifiFrequency: Int? = null,
    @SerialName("wifi_link_speed")
    val wifiLinkSpeed: Int? = null,
    @SerialName("wifi_signal_strength")
    val wifiSignalStrength: Int? = null,
)
