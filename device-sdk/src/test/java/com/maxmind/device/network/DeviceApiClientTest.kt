package com.maxmind.device.network

import com.maxmind.device.model.ServerResponse
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class DeviceApiClientTest {
    private val json = Json { ignoreUnknownKeys = true }

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
    internal fun `ApiException has correct message`() {
        val exception = DeviceApiClient.ApiException("Server returned 500: Internal Server Error")

        assertEquals("Server returned 500: Internal Server Error", exception.message)
    }
}
