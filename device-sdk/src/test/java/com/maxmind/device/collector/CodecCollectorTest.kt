package com.maxmind.device.collector

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for CodecCollector.
 *
 * Note: MediaCodecList requires Android runtime and will not work in unit tests.
 * These tests verify graceful degradation. Full codec enumeration is tested
 * via instrumented tests on real devices.
 */
internal class CodecCollectorTest {
    @Test
    internal fun `collect returns CodecInfo when MediaCodecList unavailable`() {
        // In unit tests, MediaCodecList is not available
        // The collector should gracefully return empty codec info
        val collector = CodecCollector()
        val result = collector.collect()

        assertNotNull(result)
        // Without Android runtime, we get empty lists but audio/video should be non-null
        assertNotNull(result.audio)
        assertNotNull(result.video)
    }

    @Test
    internal fun `collect returns non-null CodecInfo object`() {
        val collector = CodecCollector()
        val result = collector.collect()

        assertNotNull(result)
        assertNotNull(result.audio)
        assertNotNull(result.video)
    }
}
