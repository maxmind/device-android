package com.maxmind.device.collector

import com.maxmind.device.model.CodecInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for CodecCollector.
 *
 * Note: Full codec enumeration requires Android runtime and is tested
 * via instrumented tests on real devices. These unit tests verify
 * the basic API contract.
 */
internal class CodecCollectorTest {
    @Test
    internal fun `collector can be instantiated`() {
        val collector = CodecCollector()
        assertNotNull(collector)
    }

    @Test
    internal fun `CodecInfo default values are empty lists`() {
        val codecInfo = CodecInfo()

        assertNotNull(codecInfo.audio)
        assertNotNull(codecInfo.video)
        assertEquals(emptyList<Any>(), codecInfo.audio)
        assertEquals(emptyList<Any>(), codecInfo.video)
    }
}
