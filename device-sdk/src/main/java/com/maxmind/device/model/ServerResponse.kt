package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the MaxMind device API.
 *
 * @property storedID The server-generated stored ID (format: "{uuid}:{hmac}")
 */
@Serializable
public data class ServerResponse(
    @SerialName("stored_id")
    val storedID: String? = null,
)
