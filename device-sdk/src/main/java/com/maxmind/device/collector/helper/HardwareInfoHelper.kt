package com.maxmind.device.collector.helper

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.maxmind.device.model.HardwareInfo

/**
 * Helper class for collecting hardware information.
 *
 * Encapsulates access to [ActivityManager] and [StatFs] for testability.
 */
internal class HardwareInfoHelper(
    private val context: Context,
) {
    /**
     * Collects hardware information from the device.
     *
     * @return [HardwareInfo] containing CPU, memory, and storage details
     */
    public fun collect(): HardwareInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorageBytes = statFs.blockCountLong * statFs.blockSizeLong

        return HardwareInfo(
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalMemoryBytes = memoryInfo.totalMem,
            totalStorageBytes = totalStorageBytes,
        )
    }
}
