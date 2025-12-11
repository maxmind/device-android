package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Behavioral signals from user configuration.
 */
@Serializable
public data class BehaviorInfo(
    @SerialName("enabled_keyboards")
    val enabledKeyboards: List<String> = emptyList(),
    @SerialName("enabled_accessibility_services")
    val enabledAccessibilityServices: List<String> = emptyList(),
)
