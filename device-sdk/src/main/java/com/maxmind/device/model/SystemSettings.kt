package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * System configuration settings.
 */
@Serializable
public data class SystemSettings(
    @SerialName("screen_timeout")
    val screenTimeout: Int? = null,
    @SerialName("development_settings_enabled")
    val developmentSettingsEnabled: Boolean? = null,
    @SerialName("adb_enabled")
    val adbEnabled: Boolean? = null,
    @SerialName("animator_duration_scale")
    val animatorDurationScale: Float? = null,
    @SerialName("boot_count")
    val bootCount: Int? = null,
)
