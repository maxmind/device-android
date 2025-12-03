package com.maxmind.device.collector

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.maxmind.device.model.SensorInfo

/**
 * Collects information about device sensors.
 *
 * Enumerates all sensors available on the device and captures
 * their properties for device fingerprinting.
 */
internal class SensorCollector(
    private val context: Context,
) {
    /**
     * Collects information about all sensors on the device.
     *
     * @return List of [SensorInfo] for each available sensor
     */
    fun collect(): List<SensorInfo> {
        val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
                ?: return emptyList()

        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        return sensors.map { sensor ->
            SensorInfo(
                name = sensor.name,
                vendor = sensor.vendor,
                type = sensor.type,
                version = sensor.version,
                maxRange = sensor.maximumRange,
                resolution = sensor.resolution,
                power = sensor.power,
            )
        }
    }
}
