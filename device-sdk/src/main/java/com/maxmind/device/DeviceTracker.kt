package com.maxmind.device

import android.content.Context
import android.util.Log
import com.maxmind.device.collector.DeviceDataCollector
import com.maxmind.device.config.SdkConfig
import com.maxmind.device.model.DeviceData
import com.maxmind.device.network.DeviceApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Main entry point for the MaxMind Device Tracking SDK.
 *
 * This class provides the public API for initializing the SDK, collecting device data,
 * and sending it to MaxMind servers for device tracking and fraud detection.
 *
 * Example usage:
 * ```
 * // Initialize the SDK
 * val config = SdkConfig.Builder(123456)  // Your MaxMind account ID
 *     .enableLogging(true)
 *     .build()
 *
 * DeviceTracker.initialize(context, config)
 *
 * // Collect and send device data
 * DeviceTracker.getInstance().collectAndSend { result ->
 *     result.onSuccess {
 *         Log.d("SDK", "Data sent successfully")
 *     }.onFailure { error ->
 *         Log.e("SDK", "Failed to send data", error)
 *     }
 * }
 * ```
 */
public class DeviceTracker private constructor(
    context: Context,
    private val config: SdkConfig,
) {
    private val applicationContext: Context = context.applicationContext
    private val deviceDataCollector = DeviceDataCollector(applicationContext)
    private val apiClient =
        DeviceApiClient(
            serverUrl = config.serverUrl,
            accountID = config.accountID,
            enableLogging = config.enableLogging,
        )

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        if (config.enableLogging) {
            Log.d(TAG, "MaxMind Device Tracker initialized (version: ${BuildConfig.SDK_VERSION})")
        }

        // Start automatic collection if configured
        if (config.collectionIntervalMs > 0) {
            startAutomaticCollection()
        }
    }

    /**
     * Collects current device data synchronously.
     *
     * @return [DeviceData] containing collected device information
     */
    public fun collectDeviceData(): DeviceData {
        return deviceDataCollector.collect()
    }

    /**
     * Sends device data to MaxMind servers.
     *
     * This is a suspending function that should be called from a coroutine.
     *
     * @param deviceData The device data to send
     * @return [Result] indicating success or failure
     */
    public suspend fun sendDeviceData(deviceData: DeviceData): Result<Unit> {
        return apiClient.sendDeviceData(deviceData).map { Unit }
    }

    /**
     * Collects device data and sends it to MaxMind servers in one operation.
     *
     * This is a suspending function that should be called from a coroutine.
     *
     * @return [Result] indicating success or failure
     */
    public suspend fun collectAndSend(): Result<Unit> {
        val deviceData = collectDeviceData()
        return sendDeviceData(deviceData)
    }

    /**
     * Collects device data and sends it asynchronously with a callback.
     *
     * This is a convenience method for Java compatibility and simpler usage.
     *
     * @param callback Callback invoked when the operation completes
     */
    @JvmOverloads
    public fun collectAndSend(callback: ((Result<Unit>) -> Unit)? = null) {
        coroutineScope.launch {
            val result = collectAndSend()
            callback?.invoke(result)
        }
    }

    private fun startAutomaticCollection() {
        coroutineScope.launch {
            while (isActive) {
                try {
                    collectAndSend()
                    if (config.enableLogging) {
                        Log.d(TAG, "Automatic device data collection completed")
                    }
                } catch (e: Exception) {
                    if (config.enableLogging) {
                        Log.e(TAG, "Automatic collection failed", e)
                    }
                }
                delay(config.collectionIntervalMs)
            }
        }
    }

    /**
     * Shuts down the SDK and releases resources.
     *
     * After calling this method, the SDK instance should not be used.
     */
    public fun shutdown() {
        coroutineScope.cancel()
        apiClient.close()

        if (config.enableLogging) {
            Log.d(TAG, "MaxMind Device Tracker shut down")
        }
    }

    public companion object {
        private const val TAG = "DeviceTracker"

        @Volatile
        private var instance: DeviceTracker? = null

        /**
         * Initializes the SDK with the given configuration.
         *
         * This method should be called once during application startup.
         *
         * @param context Application or activity context
         * @param config SDK configuration
         * @return Initialized SDK instance
         * @throws IllegalStateException if SDK is already initialized
         */
        @JvmStatic
        public fun initialize(
            context: Context,
            config: SdkConfig,
        ): DeviceTracker {
            if (instance != null) {
                throw IllegalStateException("SDK is already initialized")
            }

            return synchronized(this) {
                instance ?: DeviceTracker(context, config).also { instance = it }
            }
        }

        /**
         * Returns the initialized SDK instance.
         *
         * @return SDK instance
         * @throws IllegalStateException if SDK is not initialized
         */
        @JvmStatic
        public fun getInstance(): DeviceTracker {
            return instance
                ?: throw IllegalStateException("SDK not initialized. Call initialize() first.")
        }

        /**
         * Checks if the SDK is initialized.
         *
         * @return true if initialized, false otherwise
         */
        @JvmStatic
        public fun isInitialized(): Boolean = instance != null
    }
}
