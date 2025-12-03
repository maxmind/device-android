package com.maxmind.device.collector

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NetworkCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockWifiManager: WifiManager
    private lateinit var mockNetwork: Network
    private lateinit var mockCapabilities: NetworkCapabilities
    private lateinit var collector: NetworkCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockConnectivityManager = mockk(relaxed = true)
        mockWifiManager = mockk(relaxed = true)
        mockNetwork = mockk(relaxed = true)
        mockCapabilities = mockk(relaxed = true)

        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns
            mockConnectivityManager
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.getSystemService(Context.WIFI_SERVICE) } returns mockWifiManager
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns
            mockCapabilities

        collector = NetworkCollector(mockContext)
    }

    @Test
    internal fun `collect returns wifi connection info`() {
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every {
            mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } returns true
        every { mockCapabilities.linkDownstreamBandwidthKbps } returns 100000

        val mockWifiInfo =
            mockk<WifiInfo> {
                every { frequency } returns 5180
                every { linkSpeed } returns 866
                every { rssi } returns -50
            }
        every { mockWifiManager.connectionInfo } returns mockWifiInfo

        val result = collector.collect()

        assertNotNull(result)
        assertEquals("wifi", result?.connectionType)
        assertEquals(false, result?.isMetered)
        assertEquals(100000, result?.linkDownstreamBandwidthKbps)
        assertEquals(5180, result?.wifiFrequency)
        assertEquals(866, result?.wifiLinkSpeed)
        assertEquals(-50, result?.wifiSignalStrength)
    }

    @Test
    internal fun `collect returns cellular connection info`() {
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every {
            mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } returns false
        every { mockCapabilities.linkDownstreamBandwidthKbps } returns 50000

        val result = collector.collect()

        assertNotNull(result)
        assertEquals("cellular", result?.connectionType)
        assertEquals(true, result?.isMetered)
        assertEquals(50000, result?.linkDownstreamBandwidthKbps)
        assertNull(result?.wifiFrequency)
        assertNull(result?.wifiLinkSpeed)
        assertNull(result?.wifiSignalStrength)
    }

    @Test
    internal fun `collect returns null when no active network`() {
        every { mockConnectivityManager.activeNetwork } returns null

        val result = collector.collect()

        assertNull(result)
    }

    @Test
    internal fun `collect returns null when no capabilities`() {
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns null

        val result = collector.collect()

        assertNull(result)
    }

    @Test
    internal fun `collect returns null when ConnectivityManager unavailable`() {
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null
        val collectorWithNoConnectivity = NetworkCollector(mockContext)

        val result = collectorWithNoConnectivity.collect()

        assertNull(result)
    }

    @Test
    internal fun `collect handles ethernet connection`() {
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns true
        every {
            mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } returns true
        every { mockCapabilities.linkDownstreamBandwidthKbps } returns 1000000

        val result = collector.collect()

        assertNotNull(result)
        assertEquals("ethernet", result?.connectionType)
    }

    @Test
    internal fun `connection type constants have correct values`() {
        assertEquals("wifi", NetworkCollector.CONNECTION_TYPE_WIFI)
        assertEquals("cellular", NetworkCollector.CONNECTION_TYPE_CELLULAR)
        assertEquals("ethernet", NetworkCollector.CONNECTION_TYPE_ETHERNET)
        assertEquals("bluetooth", NetworkCollector.CONNECTION_TYPE_BLUETOOTH)
        assertEquals("vpn", NetworkCollector.CONNECTION_TYPE_VPN)
        assertEquals("other", NetworkCollector.CONNECTION_TYPE_OTHER)
    }
}
