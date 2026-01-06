package com.maxmind.device.network

import com.maxmind.device.config.SdkConfig
import com.maxmind.device.model.DeviceData
import com.maxmind.device.model.ServerResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * HTTP client for communicating with MaxMind device API.
 *
 * This class handles the network communication for sending device data
 * to MaxMind servers. By default, it uses a dual-request flow:
 * 1. Send to IPv6 endpoint first
 * 2. If IPv6 succeeds and returns ip_version=6, also send to IPv4 endpoint
 *
 * This ensures both IP addresses are captured for the device.
 *
 * @param config SDK configuration
 * @param httpClient Optional HttpClient for testing (default: creates Android engine client)
 */
internal class DeviceApiClient(
    private val config: SdkConfig,
    httpClient: HttpClient? = null,
) {
    private val json =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

    private val client: HttpClient =
        httpClient ?: HttpClient(Android) {
            install(ContentNegotiation) {
                json(this@DeviceApiClient.json)
            }

            if (config.enableLogging) {
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                android.util.Log.d(TAG, message)
                            }
                        }
                    level = LogLevel.INFO
                }
            }
        }

    /**
     * Sends device data to the MaxMind API using the dual-request flow.
     *
     * If using default servers (no custom URL set):
     * 1. First sends to IPv6 endpoint
     * 2. If IPv6 response indicates ip_version=6, also sends to IPv4 endpoint
     *
     * If a custom server URL is set, sends only to that URL.
     *
     * @param deviceData The device data to send
     * @return [Result] containing the server response with stored ID, or an error
     */
    suspend fun sendDeviceData(deviceData: DeviceData): Result<ServerResponse> =
        if (config.useDefaultServers) {
            sendWithDualRequest(deviceData)
        } else {
            sendToUrl(deviceData, config.customServerUrl!! + SdkConfig.ENDPOINT_PATH)
        }

    /**
     * Sends device data using the dual-request flow (IPv6 first, then IPv4 if needed).
     */
    private suspend fun sendWithDualRequest(deviceData: DeviceData): Result<ServerResponse> {
        // First, try IPv6 - measure duration for proxy detection
        val ipv6Url = "https://${SdkConfig.DEFAULT_IPV6_HOST}${SdkConfig.ENDPOINT_PATH}"
        val startTime = System.currentTimeMillis()
        val ipv6Result = sendToUrl(deviceData, ipv6Url)
        val requestDurationMs = System.currentTimeMillis() - startTime

        if (ipv6Result.isFailure) {
            return ipv6Result
        }

        val ipv6Response = ipv6Result.getOrNull()!!

        // If we got an IPv6 response, also send to IPv4 with the request duration
        if (ipv6Response.ipVersion == IPV6) {
            val ipv4Url = "https://${SdkConfig.DEFAULT_IPV4_HOST}${SdkConfig.ENDPOINT_PATH}"
            val dataWithDuration =
                deviceData.copy(
                    requestDuration = requestDurationMs.toFloat(),
                )
            // Send to IPv4 but don't fail the overall operation if it fails
            // The stored_id from IPv6 is already valid
            sendToUrl(dataWithDuration, ipv4Url)
        }

        // Return the IPv6 response (which has the stored_id)
        return ipv6Result
    }

    /**
     * Sends device data to a specific URL.
     */
    internal suspend fun sendToUrl(
        deviceData: DeviceData,
        url: String,
    ): Result<ServerResponse> =
        try {
            // Build request body with account_id at top level, merged with device data
            val requestBody =
                buildJsonObject {
                    put("account_id", config.accountID)
                    // Merge all DeviceData fields into the request
                    json.encodeToJsonElement(deviceData).jsonObject.forEach { (key, value) ->
                        put(key, value)
                    }
                }

            val response =
                client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            if (response.status.isSuccess()) {
                val serverResponse: ServerResponse = response.body()
                Result.success(serverResponse)
            } else {
                Result.failure(
                    ApiException("Server returned ${response.status.value}: ${response.status.description}"),
                )
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            Result.failure(e)
        }

    /**
     * Exception thrown when API request fails.
     */
    class ApiException(
        message: String,
    ) : Exception(message)

    /**
     * Closes the HTTP client and releases resources.
     */
    fun close() {
        client.close()
    }

    private companion object {
        private const val TAG = "DeviceApiClient"
        private const val IPV6 = 6
    }
}
