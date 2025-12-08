package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the MaxMind device API.
 *
 * @property storedID The server-generated stored ID (format: "{uuid}:{hmac}")
 * @property ipVersion The IP version used for the request (4 or 6)
 */
@Serializable
public data class ServerResponse(
    @SerialName("stored_id")
    val storedID: String? = null,
    @SerialName("ip_version")
    val ipVersion: Int? = null,
)
