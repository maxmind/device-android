package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Font profile information based on available system fonts.
 */
@Serializable
public data class FontInfo(
    @SerialName("available_fonts")
    val availableFonts: List<String> = emptyList(),
)
