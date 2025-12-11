package com.maxmind.device.collector

import android.content.Context
import android.webkit.WebSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WebViewCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var collector: WebViewCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockkStatic(WebSettings::class)
        collector = WebViewCollector(mockContext)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkStatic(WebSettings::class)
    }

    @Test
    internal fun `collectUserAgent returns user agent string`() {
        val expectedUserAgent =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36"
        every { WebSettings.getDefaultUserAgent(mockContext) } returns expectedUserAgent

        val result = collector.collectUserAgent()

        assertEquals(expectedUserAgent, result)
    }

    @Test
    internal fun `collectUserAgent returns null when WebView unavailable`() {
        every { WebSettings.getDefaultUserAgent(mockContext) } throws RuntimeException("No WebView")

        val result = collector.collectUserAgent()

        assertNull(result)
    }
}
