package com.maxmind.device.model

import kotlinx.serialization.Serializable

/**
 * Device identifiers that persist across sessions.
 *
 * @property mediaDrmID Hardware-backed ID from MediaDRM, persists through factory reset
 * @property androidID App-scoped ID from Settings.Secure, persists across reinstalls
 */
@Serializable
public data class StoredIDs(
    val mediaDrmID: String? = null,
    val androidID: String? = null,
)
