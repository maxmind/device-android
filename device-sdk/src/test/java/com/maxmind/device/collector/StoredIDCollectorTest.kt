package com.maxmind.device.collector

import com.maxmind.device.storage.StoredIDStorage
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StoredIDCollectorTest {
    private lateinit var mockStorage: StoredIDStorage
    private lateinit var collector: StoredIDCollector

    @BeforeEach
    internal fun setUp() {
        mockStorage = mockk(relaxed = true)
        collector = StoredIDCollector(mockStorage)
    }

    @Test
    internal fun `collect returns StoredID with id when storage has value`() {
        val expectedId = "test-uuid:test-hmac"
        every { mockStorage.get() } returns expectedId

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(expectedId, result.id)
    }

    @Test
    internal fun `collect returns StoredID with null id when storage is empty`() {
        every { mockStorage.get() } returns null

        val result = collector.collect()

        assertNotNull(result)
        assertNull(result.id)
    }
}
