package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device-generated identifiers that persist across sessions.
 *
 * These are hardware/system identifiers generated on the device itself,
 * distinct from server-generated stored IDs.
 *
 * @property mediaDrmID Hardware-backed ID from MediaDRM, persists through factory reset
 * @property androidID App-scoped ID from Settings.Secure, persists across reinstalls
 */
@Serializable
public data class DeviceIDs(
    @SerialName("media_drm_id")
    val mediaDrmID: String? = null,
    @SerialName("android_id")
    val androidID: String? = null,
)
