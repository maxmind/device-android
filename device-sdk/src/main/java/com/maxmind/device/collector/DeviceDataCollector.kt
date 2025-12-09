package com.maxmind.device.collector

import android.content.Context
import android.util.Log
import com.maxmind.device.collector.helper.BuildInfoHelper
import com.maxmind.device.collector.helper.DisplayInfoHelper
import com.maxmind.device.collector.helper.HardwareInfoHelper
import com.maxmind.device.collector.helper.InstallationInfoHelper
import com.maxmind.device.collector.helper.LocaleInfoHelper
import com.maxmind.device.model.BuildInfo
import com.maxmind.device.model.DeviceData
import com.maxmind.device.model.DisplayInfo
import com.maxmind.device.model.HardwareInfo
import com.maxmind.device.model.InstallationInfo
import com.maxmind.device.model.LocaleInfo
import com.maxmind.device.model.StoredID
import com.maxmind.device.storage.StoredIDStorage
import java.util.TimeZone

/**
 * Collects device information from the Android system.
 *
 * This class is responsible for gathering various device attributes
 * that are available through the Android APIs.
 *
 * @param context Application context for accessing system services
 * @param storedIDStorage Optional storage for server-generated stored IDs
 * @param enableLogging Whether to log collection failures (defaults to false)
 * @param buildInfoHelper Helper for collecting build info (injectable for testing)
 * @param displayInfoHelper Helper for collecting display info (injectable for testing)
 * @param hardwareInfoHelper Helper for collecting hardware info (injectable for testing)
 * @param installationInfoHelper Helper for collecting installation info (injectable for testing)
 * @param localeInfoHelper Helper for collecting locale info (injectable for testing)
 */
@Suppress("LongParameterList") // Intentional for dependency injection/testing
internal class DeviceDataCollector(
    context: Context,
    storedIDStorage: StoredIDStorage? = null,
    private val enableLogging: Boolean = false,
    private val buildInfoHelper: BuildInfoHelper = BuildInfoHelper(),
    private val displayInfoHelper: DisplayInfoHelper = DisplayInfoHelper(context),
    private val hardwareInfoHelper: HardwareInfoHelper = HardwareInfoHelper(context),
    private val installationInfoHelper: InstallationInfoHelper = InstallationInfoHelper(context),
    private val localeInfoHelper: LocaleInfoHelper = LocaleInfoHelper(),
) {
    private companion object {
        private const val TAG = "DeviceDataCollector"

        // Fallback values for when collection fails
        private val BUILD_INFO_FALLBACK =
            BuildInfo(
                fingerprint = "",
                manufacturer = "",
                model = "",
                brand = "",
                device = "",
                product = "",
                board = "",
                hardware = "",
                osVersion = "",
                sdkVersion = 0,
            )

        private val DISPLAY_INFO_FALLBACK =
            DisplayInfo(
                widthPixels = 0,
                heightPixels = 0,
                densityDpi = 0,
                density = 0f,
            )

        private val HARDWARE_INFO_FALLBACK =
            HardwareInfo(
                cpuCores = 0,
                totalMemoryBytes = 0L,
                totalStorageBytes = 0L,
            )

        private val INSTALLATION_INFO_FALLBACK =
            InstallationInfo(
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                versionCode = 0L,
            )

        private val LOCALE_INFO_FALLBACK =
            LocaleInfo(
                language = "",
                country = "",
                timezone = "",
            )
    }

    private val storedIDCollector = storedIDStorage?.let { StoredIDCollector(it) }
    private val deviceIDsCollector = DeviceIDsCollector(context)
    private val gpuCollector = GpuCollector()
    private val audioCollector = AudioCollector(context)
    private val sensorCollector = SensorCollector(context)
    private val cameraCollector = CameraCollector(context)
    private val codecCollector = CodecCollector()
    private val systemFeaturesCollector = SystemFeaturesCollector(context)
    private val networkCollector = NetworkCollector(context)
    private val settingsCollector = SettingsCollector(context)
    private val behaviorCollector = BehaviorCollector(context)
    private val telephonyCollector = TelephonyCollector(context)
    private val fontCollector = FontCollector()
    private val webViewCollector = WebViewCollector(context)

    /**
     * Safely executes a collection block, returning a fallback value on failure.
     *
     * This ensures partial data collection even if individual subsystems fail.
     *
     * @param fallback The value to return if collection fails
     * @param block The collection block to execute
     * @return The collected value or fallback on failure
     */
    @Suppress("TooGenericExceptionCaught")
    private inline fun <T> collectSafe(
        fallback: T,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (e: Exception) {
            if (enableLogging) {
                Log.w(TAG, "Collection failed: ${e.message}", e)
            }
            fallback
        }

    /**
     * Collects current device data.
     *
     * @return [DeviceData] containing collected device information
     */
    public fun collect(): DeviceData =
        DeviceData(
            storedID = storedIDCollector?.collect() ?: StoredID(),
            deviceIDs = deviceIDsCollector.collect(),
            build = collectSafe(BUILD_INFO_FALLBACK) { buildInfoHelper.collect() },
            display = collectSafe(DISPLAY_INFO_FALLBACK) { displayInfoHelper.collect() ?: DISPLAY_INFO_FALLBACK },
            hardware = collectSafe(HARDWARE_INFO_FALLBACK) { hardwareInfoHelper.collect() },
            gpu = gpuCollector.collect(),
            audio = audioCollector.collect(),
            sensors = sensorCollector.collect(),
            cameras = cameraCollector.collect(),
            codecs = codecCollector.collect(),
            systemFeatures = systemFeaturesCollector.collect(),
            network = networkCollector.collect(),
            installation = collectSafe(INSTALLATION_INFO_FALLBACK) { installationInfoHelper.collect() },
            settings = settingsCollector.collect(),
            behavior = behaviorCollector.collect(),
            telephony = telephonyCollector.collect(),
            fonts = fontCollector.collect(),
            locale = collectSafe(LOCALE_INFO_FALLBACK) { localeInfoHelper.collect() },
            // Timezone offset in minutes (uses getOffset to account for DST)
            timezoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60000,
            deviceTime = System.currentTimeMillis(),
            webViewUserAgent = webViewCollector.collectUserAgent(),
        )
}
