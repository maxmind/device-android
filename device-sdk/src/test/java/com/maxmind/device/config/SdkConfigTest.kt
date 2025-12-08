package com.maxmind.device.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SdkConfigTest {
    @Test
    internal fun `build with valid account ID creates config with defaults`() {
        val config = SdkConfig.Builder(12345).build()

        assertEquals(12345, config.accountID)
        assertNull(config.customServerUrl)
        assertTrue(config.useDefaultServers)
        assertFalse(config.enableLogging)
        assertEquals(0L, config.collectionIntervalMs)
    }

    @Test
    internal fun `build with custom server URL uses custom URL`() {
        val customUrl = "https://custom.maxmind.com/api"
        val config =
            SdkConfig
                .Builder(12345)
                .serverUrl(customUrl)
                .build()

        assertEquals(customUrl, config.customServerUrl)
        assertFalse(config.useDefaultServers)
    }

    @Test
    internal fun `build with logging enabled sets logging flag`() {
        val config =
            SdkConfig
                .Builder(12345)
                .enableLogging(true)
                .build()

        assertEquals(true, config.enableLogging)
    }

    @Test
    internal fun `build with collection interval sets interval`() {
        val config =
            SdkConfig
                .Builder(12345)
                .collectionInterval(60000)
                .build()

        assertEquals(60000L, config.collectionIntervalMs)
    }

    @Test
    internal fun `build with all custom values applies all settings`() {
        val config =
            SdkConfig
                .Builder(67890)
                .serverUrl("https://example.com/api")
                .enableLogging(true)
                .collectionInterval(30000)
                .build()

        assertEquals(67890, config.accountID)
        assertEquals("https://example.com/api", config.customServerUrl)
        assertFalse(config.useDefaultServers)
        assertEquals(true, config.enableLogging)
        assertEquals(30000L, config.collectionIntervalMs)
    }

    @Test
    internal fun `build with zero account ID throws exception`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                SdkConfig.Builder(0).build()
            }
        assertEquals("Account ID must be positive", exception.message)
    }

    @Test
    internal fun `build with negative account ID throws exception`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                SdkConfig.Builder(-1).build()
            }
        assertEquals("Account ID must be positive", exception.message)
    }

    @Test
    internal fun `build with blank server URL throws exception`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                SdkConfig
                    .Builder(12345)
                    .serverUrl("")
                    .build()
            }
        assertEquals("Server URL cannot be blank", exception.message)
    }

    @Test
    internal fun `collectionInterval with negative value throws exception`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                SdkConfig
                    .Builder(12345)
                    .collectionInterval(-1)
            }
        assertEquals("Collection interval must be non-negative", exception.message)
    }

    @Test
    internal fun `collectionInterval with zero is valid`() {
        val config =
            SdkConfig
                .Builder(12345)
                .collectionInterval(0)
                .build()

        assertEquals(0L, config.collectionIntervalMs)
    }

    @Test
    internal fun `default hosts constants are correct`() {
        assertEquals("d-ipv6.mmapiws.com", SdkConfig.DEFAULT_IPV6_HOST)
        assertEquals("d-ipv4.mmapiws.com", SdkConfig.DEFAULT_IPV4_HOST)
        assertEquals("/device/android", SdkConfig.ENDPOINT_PATH)
    }

    @Test
    internal fun `useDefaultServers is true when customServerUrl is null`() {
        val config = SdkConfig.Builder(12345).build()

        assertTrue(config.useDefaultServers)
        assertNull(config.customServerUrl)
    }

    @Test
    internal fun `useDefaultServers is false when customServerUrl is set`() {
        val config =
            SdkConfig
                .Builder(12345)
                .serverUrl("https://custom.example.com")
                .build()

        assertFalse(config.useDefaultServers)
        assertEquals("https://custom.example.com", config.customServerUrl)
    }
}
