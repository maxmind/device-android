package com.maxmind.device.collector

import android.content.Context
import android.telephony.TelephonyManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TelephonyCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockTelephonyManager: TelephonyManager
    private lateinit var collector: TelephonyCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockTelephonyManager = mockk(relaxed = true)

        every { mockContext.getSystemService(Context.TELEPHONY_SERVICE) } returns mockTelephonyManager

        collector = TelephonyCollector(mockContext)
    }

    @Test
    internal fun `collect returns telephony info with carrier name`() {
        every { mockTelephonyManager.networkOperatorName } returns "Test Carrier"
        every { mockTelephonyManager.simState } returns TelephonyManager.SIM_STATE_READY
        every { mockTelephonyManager.phoneType } returns TelephonyManager.PHONE_TYPE_GSM
        every { mockTelephonyManager.hasIccCard() } returns true

        val result = collector.collect()

        assertNotNull(result)
        assertEquals("Test Carrier", result?.networkOperatorName)
        assertEquals(TelephonyManager.SIM_STATE_READY, result?.simState)
        assertEquals(TelephonyManager.PHONE_TYPE_GSM, result?.phoneType)
        assertTrue(result?.hasIccCard == true)
    }

    @Test
    internal fun `collect returns null network operator name for blank string`() {
        every { mockTelephonyManager.networkOperatorName } returns "   "
        every { mockTelephonyManager.simState } returns TelephonyManager.SIM_STATE_ABSENT
        every { mockTelephonyManager.phoneType } returns TelephonyManager.PHONE_TYPE_NONE
        every { mockTelephonyManager.hasIccCard() } returns false

        val result = collector.collect()

        assertNotNull(result)
        assertNull(result?.networkOperatorName)
        assertEquals(TelephonyManager.SIM_STATE_ABSENT, result?.simState)
        assertEquals(TelephonyManager.PHONE_TYPE_NONE, result?.phoneType)
        assertEquals(false, result?.hasIccCard)
    }

    @Test
    internal fun `collect returns null when TelephonyManager unavailable`() {
        every { mockContext.getSystemService(Context.TELEPHONY_SERVICE) } returns null
        val collectorWithNoTelephony = TelephonyCollector(mockContext)

        val result = collectorWithNoTelephony.collect()

        assertNull(result)
    }

    @Test
    internal fun `collect handles exception gracefully`() {
        every { mockTelephonyManager.networkOperatorName } throws RuntimeException("Test exception")

        val result = collector.collect()

        assertNull(result)
    }

    @Test
    internal fun `collect returns CDMA phone type`() {
        every { mockTelephonyManager.networkOperatorName } returns "CDMA Carrier"
        every { mockTelephonyManager.simState } returns TelephonyManager.SIM_STATE_READY
        every { mockTelephonyManager.phoneType } returns TelephonyManager.PHONE_TYPE_CDMA
        every { mockTelephonyManager.hasIccCard() } returns true

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(TelephonyManager.PHONE_TYPE_CDMA, result?.phoneType)
    }
}
