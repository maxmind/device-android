package com.maxmind.device.network

import com.maxmind.device.config.SdkConfig
import com.maxmind.device.model.BuildInfo
import com.maxmind.device.model.DeviceData
import com.maxmind.device.model.DisplayInfo
import com.maxmind.device.model.HardwareInfo
import com.maxmind.device.model.InstallationInfo
import com.maxmind.device.model.LocaleInfo
import com.maxmind.device.model.ServerResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

internal class DeviceApiClientTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val testDeviceData =
        DeviceData(
            build =
                BuildInfo(
                    fingerprint = "test/fingerprint",
                    manufacturer = "TestManufacturer",
                    model = "TestModel",
                    brand = "TestBrand",
                    device = "testdevice",
                    product = "testproduct",
                    board = "testboard",
                    hardware = "testhardware",
                    osVersion = "14",
                    sdkVersion = 34,
                ),
            display =
                DisplayInfo(
                    widthPixels = 1080,
                    heightPixels = 1920,
                    densityDpi = 420,
                    density = 2.625f,
                ),
            hardware =
                HardwareInfo(
                    cpuCores = 8,
                    totalMemoryBytes = 8_000_000_000L,
                    totalStorageBytes = 128_000_000_000L,
                ),
            installation =
                InstallationInfo(
                    firstInstallTime = 1700000000000L,
                    lastUpdateTime = 1700000000000L,
                    versionCode = 1L,
                ),
            locale =
                LocaleInfo(
                    language = "en",
                    country = "US",
                    timezone = "America/New_York",
                ),
            timezoneOffset = -300,
        )

    // ========== ServerResponse Parsing Tests ==========

    @Test
    internal fun `ServerResponse parses stored_id correctly`() {
        val jsonString = """{"stored_id":"test-uuid:test-hmac"}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertEquals("test-uuid:test-hmac", response.storedID)
    }

    @Test
    internal fun `ServerResponse parses null stored_id correctly`() {
        val jsonString = """{"stored_id":null}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertNull(response.storedID)
    }

    @Test
    internal fun `ServerResponse parses missing stored_id as null`() {
        val jsonString = """{}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertNull(response.storedID)
    }

    @Test
    internal fun `ServerResponse ignores unknown fields`() {
        val jsonString =
            """{"stored_id":"test-uuid:test-hmac","unknown_field":"value","status":"ok"}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertEquals("test-uuid:test-hmac", response.storedID)
    }

    @Test
    internal fun `ServerResponse parses ip_version correctly`() {
        val jsonString = """{"stored_id":"test-uuid:test-hmac","ip_version":6}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertEquals("test-uuid:test-hmac", response.storedID)
        assertEquals(6, response.ipVersion)
    }

    @Test
    internal fun `ServerResponse parses ip_version 4 correctly`() {
        val jsonString = """{"stored_id":"test-uuid:test-hmac","ip_version":4}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertEquals(4, response.ipVersion)
    }

    @Test
    internal fun `ServerResponse parses missing ip_version as null`() {
        val jsonString = """{"stored_id":"test-uuid:test-hmac"}"""

        val response = json.decodeFromString<ServerResponse>(jsonString)

        assertNotNull(response)
        assertNull(response.ipVersion)
    }

    @Test
    internal fun `ApiException has correct message`() {
        val exception = DeviceApiClient.ApiException("Server returned 500: Internal Server Error")

        assertEquals("Server returned 500: Internal Server Error", exception.message)
    }

    // ========== MockEngine HTTP Tests ==========

    @Test
    internal fun `sendDeviceData returns success with stored_id`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"stored_id":"abc123:hmac456"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine)

            val result = client.sendDeviceData(testDeviceData)

            assertTrue(result.isSuccess)
            assertEquals("abc123:hmac456", result.getOrNull()?.storedID)
            client.close()
        }

    @Test
    internal fun `sendDeviceData returns success with null stored_id`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"stored_id":null}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine)

            val result = client.sendDeviceData(testDeviceData)

            assertTrue(result.isSuccess)
            assertNull(result.getOrNull()?.storedID)
            client.close()
        }

    @Test
    internal fun `sendDeviceData returns failure on server error`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"error":"Internal Server Error"}""",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine)

            val result = client.sendDeviceData(testDeviceData)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is DeviceApiClient.ApiException)
            assertTrue(exception?.message?.contains("500") == true)
            client.close()
        }

    @Test
    internal fun `sendDeviceData returns failure on client error`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"error":"Bad Request"}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine)

            val result = client.sendDeviceData(testDeviceData)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is DeviceApiClient.ApiException)
            assertTrue(exception?.message?.contains("400") == true)
            client.close()
        }

    @Test
    internal fun `sendDeviceData sends correct request body`() =
        runTest {
            var capturedBody: String? = null
            val mockEngine =
                MockEngine { request ->
                    capturedBody = (request.body as TextContent).text
                    respond(
                        content = """{"stored_id":"test"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine, accountID = 12345)

            client.sendDeviceData(testDeviceData)

            assertNotNull(capturedBody)
            val requestJson = json.parseToJsonElement(capturedBody!!).jsonObject
            // Verify account_id is present at top level
            assertEquals("12345", requestJson["account_id"]?.jsonPrimitive?.content)
            // Verify device data fields are present
            assertNotNull(requestJson["build"])
            assertNotNull(requestJson["display"])
            assertNotNull(requestJson["hardware"])
            client.close()
        }

    @Test
    internal fun `sendDeviceData sends to correct endpoint`() =
        runTest {
            var capturedUrl: String? = null
            val mockEngine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"stored_id":"test"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine, serverUrl = "https://api.example.com")

            client.sendDeviceData(testDeviceData)

            assertEquals("https://api.example.com/device/android", capturedUrl)
            client.close()
        }

    @Test
    internal fun `sendDeviceData sets correct content type`() =
        runTest {
            var capturedContentType: io.ktor.http.ContentType? = null
            val mockEngine =
                MockEngine { request ->
                    capturedContentType = request.body.contentType
                    respond(
                        content = """{"stored_id":"test"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClient(mockEngine)

            client.sendDeviceData(testDeviceData)

            assertNotNull(capturedContentType)
            assertEquals(io.ktor.http.ContentType.Application.Json, capturedContentType?.withoutParameters())
            client.close()
        }

    @Test
    internal fun `sendDeviceData handles network exception`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    throw IOException("Network unavailable")
                }
            val client = createTestClient(mockEngine)

            val result = client.sendDeviceData(testDeviceData)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
            client.close()
        }

    // ========== Dual Request / Request Duration Tests ==========

    @Test
    internal fun `dual request includes request_duration only on second request`() =
        runTest {
            val capturedRequests = mutableListOf<Pair<String, String>>()
            var requestCount = 0

            val mockEngine =
                MockEngine { request ->
                    requestCount++
                    capturedRequests.add(request.url.toString() to (request.body as TextContent).text)
                    respond(
                        content =
                            if (requestCount == 1) {
                                """{"stored_id":"test","ip_version":6}"""
                            } else {
                                """{"stored_id":"test"}"""
                            },
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClientWithDefaultServers(mockEngine)

            client.sendDeviceData(testDeviceData)

            assertEquals(2, capturedRequests.size)

            // First request (IPv6) should NOT have request_duration
            val firstRequestJson = json.parseToJsonElement(capturedRequests[0].second).jsonObject
            assertNull(firstRequestJson["request_duration"])

            // Second request (IPv4) SHOULD have request_duration
            val secondRequestJson = json.parseToJsonElement(capturedRequests[1].second).jsonObject
            assertNotNull(secondRequestJson["request_duration"])
            assertTrue(secondRequestJson["request_duration"]!!.jsonPrimitive.float >= 0)
            client.close()
        }

    @Test
    internal fun `dual request sends to correct IPv6 and IPv4 endpoints`() =
        runTest {
            val capturedUrls = mutableListOf<String>()
            var requestCount = 0

            val mockEngine =
                MockEngine { request ->
                    requestCount++
                    capturedUrls.add(request.url.toString())
                    respond(
                        content =
                            if (requestCount == 1) {
                                """{"stored_id":"test","ip_version":6}"""
                            } else {
                                """{"stored_id":"test"}"""
                            },
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClientWithDefaultServers(mockEngine)

            client.sendDeviceData(testDeviceData)

            assertEquals(2, capturedUrls.size)
            assertTrue(capturedUrls[0].contains("d-ipv6.mmapiws.com"))
            assertTrue(capturedUrls[1].contains("d-ipv4.mmapiws.com"))
            client.close()
        }

    @Test
    internal fun `dual request skips IPv4 when ip_version is not 6`() =
        runTest {
            var requestCount = 0

            val mockEngine =
                MockEngine { _ ->
                    requestCount++
                    respond(
                        content = """{"stored_id":"test","ip_version":4}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = createTestClientWithDefaultServers(mockEngine)

            client.sendDeviceData(testDeviceData)

            assertEquals(1, requestCount)
            client.close()
        }

    // ========== Helper Functions ==========

    private fun createTestClient(
        mockEngine: MockEngine,
        serverUrl: String = "https://test.maxmind.com",
        accountID: Int = 99999,
    ): DeviceApiClient {
        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }
        val config =
            SdkConfig
                .Builder(accountID)
                .serverUrl(serverUrl)
                .build()
        return DeviceApiClient(config, httpClient)
    }

    private fun createTestClientWithDefaultServers(
        mockEngine: MockEngine,
        accountID: Int = 99999,
    ): DeviceApiClient {
        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }
        val config =
            SdkConfig
                .Builder(accountID)
                .build()
        return DeviceApiClient(config, httpClient)
    }
}
