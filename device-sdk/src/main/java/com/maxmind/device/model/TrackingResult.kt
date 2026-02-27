package com.maxmind.device.model

/**
 * Result of a device tracking operation.
 *
 * @property trackingToken Opaque token to pass to the minFraud API's
 *     `/device/tracking_token` field. Do not parse this value or rely on
 *     its format, which may change without notice.
 */
public data class TrackingResult(
    val trackingToken: String,
) {
    init {
        require(trackingToken.isNotBlank()) { "Tracking token must not be blank" }
    }

    override fun toString(): String = "TrackingResult(trackingToken=<redacted>)"
}
