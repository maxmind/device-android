package com.maxmind.device.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class TrackingResultTest {
    @Test
    internal fun `valid token creates TrackingResult`() {
        val result = TrackingResult(trackingToken = "abc123:hmac456")

        assertEquals("abc123:hmac456", result.trackingToken)
    }

    @Test
    internal fun `empty string throws IllegalArgumentException`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                TrackingResult(trackingToken = "")
            }

        assertEquals("Tracking token must not be blank", exception.message)
    }

    @Test
    internal fun `blank whitespace-only string throws IllegalArgumentException`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                TrackingResult(trackingToken = "   ")
            }

        assertEquals("Tracking token must not be blank", exception.message)
    }

    @Test
    internal fun `toString redacts tracking token`() {
        val result = TrackingResult(trackingToken = "abc123:hmac456")

        assertEquals("TrackingResult(trackingToken=<redacted>)", result.toString())
    }
}
