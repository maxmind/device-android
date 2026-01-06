package com.maxmind.device.config

/**
 * Configuration for the MaxMind Device SDK.
 *
 * Use [SdkConfig.Builder] to create instances of this class.
 *
 * @property accountID MaxMind account ID for identifying the account
 * @property customServerUrl Custom server URL (null = use default IPv6/IPv4 dual-request)
 * @property enableLogging Enable debug logging for the SDK
 * @property collectionIntervalMs Interval in milliseconds for automatic data collection (0 = disabled)
 */
public data class SdkConfig internal constructor(
    val accountID: Int,
    val customServerUrl: String? = null,
    val enableLogging: Boolean = false,
    val collectionIntervalMs: Long = 0,
) {
    /**
     * Whether to use the default dual-request flow (IPv6 then IPv4).
     * Returns true when no custom server URL is set.
     */
    val useDefaultServers: Boolean
        get() = customServerUrl == null

    /**
     * Builder for creating [SdkConfig] instances.
     *
     * Example usage:
     * ```
     * val config = SdkConfig.Builder(123456)
     *     .serverUrl("https://custom.maxmind.com/api") // Optional: override default servers
     *     .enableLogging(true)
     *     .collectionInterval(60_000) // Collect every 60 seconds
     *     .build()
     * ```
     */
    public class Builder(
        private val accountID: Int,
    ) {
        private var customServerUrl: String? = null
        private var enableLogging: Boolean = false
        private var collectionIntervalMs: Long = 0

        /**
         * Set a custom server URL for the MaxMind API endpoint.
         *
         * If not set, the SDK will use the default dual-request flow:
         * 1. First request to IPv6 server (d-ipv6.mmapiws.com)
         * 2. If IPv6 succeeds, also request to IPv4 server (d-ipv4.mmapiws.com)
         *
         * @param url Custom server URL (e.g., "https://custom.example.com")
         */
        public fun serverUrl(url: String): Builder =
            apply {
                this.customServerUrl = url
            }

        /**
         * Enable or disable debug logging.
         *
         * @param enabled true to enable logging, false to disable
         */
        public fun enableLogging(enabled: Boolean): Builder =
            apply {
                this.enableLogging = enabled
            }

        /**
         * Set the automatic data collection interval.
         *
         * @param intervalMs Interval in milliseconds (0 to disable automatic collection)
         */
        public fun collectionInterval(intervalMs: Long): Builder =
            apply {
                require(intervalMs >= 0) { "Collection interval must be non-negative" }
                this.collectionIntervalMs = intervalMs
            }

        /**
         * Build the [SdkConfig] instance.
         */
        public fun build(): SdkConfig {
            require(accountID > 0) { "Account ID must be positive" }
            customServerUrl?.let {
                require(it.isNotBlank()) { "Server URL cannot be blank" }
            }

            return SdkConfig(
                accountID = accountID,
                customServerUrl = customServerUrl,
                enableLogging = enableLogging,
                collectionIntervalMs = collectionIntervalMs,
            )
        }
    }

    public companion object {
        /** Default IPv6 server host */
        public const val DEFAULT_IPV6_HOST: String = "d-ipv6.mmapiws.com"

        /** Default IPv4 server host */
        public const val DEFAULT_IPV4_HOST: String = "d-ipv4.mmapiws.com"

        /** API endpoint path */
        public const val ENDPOINT_PATH: String = "/device/android"
    }
}
