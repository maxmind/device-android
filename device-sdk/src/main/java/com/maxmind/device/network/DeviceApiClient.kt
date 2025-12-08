package com.maxmind.device.network

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
 * to MaxMind servers.
 *
 * @param serverUrl Base URL for the MaxMind device API
 * @param accountID MaxMind account ID
 * @param enableLogging Whether to enable HTTP logging (default: false)
 * @param httpClient Optional HttpClient for testing (default: creates Android engine client)
 */
internal class DeviceApiClient(
    private val serverUrl: String,
    private val accountID: Int,
    enableLogging: Boolean = false,
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

            if (enableLogging) {
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
     * Sends device data to the MaxMind API.
     *
     * @param deviceData The device data to send
     * @return [Result] containing the server response with stored ID, or an error
     */
    suspend fun sendDeviceData(deviceData: DeviceData): Result<ServerResponse> =
        try {
            // Build request body with account_id at top level, merged with device data
            val requestBody =
                buildJsonObject {
                    put("account_id", accountID)
                    // Merge all DeviceData fields into the request
                    json.encodeToJsonElement(deviceData).jsonObject.forEach { (key, value) ->
                        put(key, value)
                    }
                }

            val response =
                client.post("$serverUrl/android/device") {
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
    }
}
