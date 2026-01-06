package com.maxmind.device.collector

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.maxmind.device.model.NetworkInfo

/**
 * Collects network context information.
 *
 * Requires ACCESS_NETWORK_STATE and ACCESS_WIFI_STATE permissions.
 */
internal class NetworkCollector(
    private val context: Context,
    private val enableLogging: Boolean = false,
) {
    /**
     * Collects current network information.
     *
     * @return [NetworkInfo] containing network context, or null if unavailable
     */
    @Suppress("ReturnCount")
    fun collect(): NetworkInfo? {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return null

            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null

            val connectionType = getConnectionType(capabilities)
            val isMetered =
                !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps

            // Get WiFi-specific info if connected to WiFi
            val (wifiFrequency, wifiLinkSpeed, wifiSignalStrength) =
                if (connectionType == CONNECTION_TYPE_WIFI) {
                    getWifiInfo()
                } else {
                    Triple(null, null, null)
                }

            NetworkInfo(
                connectionType = connectionType,
                isMetered = isMetered,
                linkDownstreamBandwidthKbps = downstreamBandwidth,
                wifiFrequency = wifiFrequency,
                wifiLinkSpeed = wifiLinkSpeed,
                wifiSignalStrength = wifiSignalStrength,
            )
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // Network info may fail on some devices or when permissions are missing
            if (enableLogging) {
                Log.d(TAG, "Failed to collect network info: ${e.message}")
            }
            null
        }
    }

    private fun getConnectionType(capabilities: NetworkCapabilities): String =
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> CONNECTION_TYPE_WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> CONNECTION_TYPE_CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> CONNECTION_TYPE_ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> CONNECTION_TYPE_BLUETOOTH
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> CONNECTION_TYPE_VPN
            else -> CONNECTION_TYPE_OTHER
        }

    @Suppress("DEPRECATION")
    private fun getWifiInfo(): Triple<Int?, Int?, Int?> {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return Triple(null, null, null)

        val wifiInfo: WifiInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // On Android 12+, connectionInfo is deprecated
                // We would need to use NetworkCallback for WiFi info
                // For simplicity, we still try connectionInfo but it may be empty
                wifiManager.connectionInfo
            } else {
                wifiManager.connectionInfo
            }

        val frequency = if (wifiInfo.frequency > 0) wifiInfo.frequency else null
        val linkSpeed = if (wifiInfo.linkSpeed > 0) wifiInfo.linkSpeed else null
        val rssi = if (wifiInfo.rssi != INVALID_RSSI) wifiInfo.rssi else null

        return Triple(frequency, linkSpeed, rssi)
    }

    internal companion object {
        private const val TAG = "NetworkCollector"
        const val CONNECTION_TYPE_WIFI = "wifi"
        const val CONNECTION_TYPE_CELLULAR = "cellular"
        const val CONNECTION_TYPE_ETHERNET = "ethernet"
        const val CONNECTION_TYPE_BLUETOOTH = "bluetooth"
        const val CONNECTION_TYPE_VPN = "vpn"
        const val CONNECTION_TYPE_OTHER = "other"

        // Invalid RSSI value (WifiManager.INVALID_RSSI is not available in all API levels)
        private const val INVALID_RSSI = -127
    }
}
