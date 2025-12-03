package com.maxmind.device.config

/**
 * Configuration for the MaxMind Device SDK.
 *
 * Use [SdkConfig.Builder] to create instances of this class.
 *
 * @property accountID MaxMind account ID for identifying the account
 * @property serverUrl Base URL for the MaxMind API endpoint
 * @property enableLogging Enable debug logging for the SDK
 * @property collectionIntervalMs Interval in milliseconds for automatic data collection (0 = disabled)
 */
public data class SdkConfig internal constructor(
    val accountID: Int,
    val serverUrl: String = DEFAULT_SERVER_URL,
    val enableLogging: Boolean = false,
    val collectionIntervalMs: Long = 0,
) {
    /**
     * Builder for creating [SdkConfig] instances.
     *
     * Example usage:
     * ```
     * val config = SdkConfig.Builder(123456)
     *     .serverUrl("https://custom.maxmind.com/api")
     *     .enableLogging(true)
     *     .collectionInterval(60_000) // Collect every 60 seconds
     *     .build()
     * ```
     */
    public class Builder(private val accountID: Int) {
        private var serverUrl: String = DEFAULT_SERVER_URL
        private var enableLogging: Boolean = false
        private var collectionIntervalMs: Long = 0

        /**
         * Set the server URL for the MaxMind API endpoint.
         *
         * @param url Base URL (e.g., "https://api.maxmind.com/device")
         */
        public fun serverUrl(url: String): Builder =
            apply {
                this.serverUrl = url
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
            require(serverUrl.isNotBlank()) { "Server URL cannot be blank" }

            return SdkConfig(
                accountID = accountID,
                serverUrl = serverUrl,
                enableLogging = enableLogging,
                collectionIntervalMs = collectionIntervalMs,
            )
        }
    }

    public companion object {
        /**
         * Default MaxMind server URL.
         */
        public const val DEFAULT_SERVER_URL: String = "https://device-api.maxmind.com/v1"
    }
}
