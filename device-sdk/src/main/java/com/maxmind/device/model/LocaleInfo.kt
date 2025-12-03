package com.maxmind.device.model

import kotlinx.serialization.Serializable

/**
 * Locale and regional information.
 */
@Serializable
public data class LocaleInfo(
    val language: String,
    val country: String,
    val timezone: String,
)
