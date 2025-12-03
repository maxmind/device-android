package com.maxmind.device.collector

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SensorCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockSensorManager: SensorManager
    private lateinit var collector: SensorCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockSensorManager = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager
        collector = SensorCollector(mockContext)
    }

    @Test
    internal fun `collect returns sensor list with properties`() {
        val mockSensor =
            mockk<Sensor> {
                every { name } returns "Accelerometer"
                every { vendor } returns "Qualcomm"
                every { type } returns Sensor.TYPE_ACCELEROMETER
                every { version } returns 1
                every { maximumRange } returns 39.2266f
                every { resolution } returns 0.0012f
                every { power } returns 0.13f
            }
        every { mockSensorManager.getSensorList(Sensor.TYPE_ALL) } returns listOf(mockSensor)

        val result = collector.collect()

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("Accelerometer", result[0].name)
        assertEquals("Qualcomm", result[0].vendor)
        assertEquals(Sensor.TYPE_ACCELEROMETER, result[0].type)
        assertEquals(1, result[0].version)
        assertEquals(39.2266f, result[0].maxRange)
        assertEquals(0.0012f, result[0].resolution)
        assertEquals(0.13f, result[0].power)
    }

    @Test
    internal fun `collect returns multiple sensors`() {
        val accelerometer =
            mockk<Sensor> {
                every { name } returns "Accelerometer"
                every { vendor } returns "Qualcomm"
                every { type } returns Sensor.TYPE_ACCELEROMETER
                every { version } returns 1
                every { maximumRange } returns 39.2266f
                every { resolution } returns 0.0012f
                every { power } returns 0.13f
            }
        val gyroscope =
            mockk<Sensor> {
                every { name } returns "Gyroscope"
                every { vendor } returns "STMicroelectronics"
                every { type } returns Sensor.TYPE_GYROSCOPE
                every { version } returns 2
                every { maximumRange } returns 34.9066f
                every { resolution } returns 0.001f
                every { power } returns 0.3f
            }
        every { mockSensorManager.getSensorList(Sensor.TYPE_ALL) } returns
            listOf(
                accelerometer,
                gyroscope,
            )

        val result = collector.collect()

        assertEquals(2, result.size)
        assertEquals("Accelerometer", result[0].name)
        assertEquals("Gyroscope", result[1].name)
    }

    @Test
    internal fun `collect returns empty list when no sensors available`() {
        every { mockSensorManager.getSensorList(Sensor.TYPE_ALL) } returns emptyList()

        val result = collector.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    internal fun `collect returns empty list when SensorManager unavailable`() {
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns null
        val collectorWithNoSensorManager = SensorCollector(mockContext)

        val result = collectorWithNoSensorManager.collect()

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}
