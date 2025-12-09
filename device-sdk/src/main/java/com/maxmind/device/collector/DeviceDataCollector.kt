package com.maxmind.device.collector

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import com.maxmind.device.model.BuildInfo
import com.maxmind.device.model.DeviceData
import com.maxmind.device.model.DisplayInfo
import com.maxmind.device.model.HardwareInfo
import com.maxmind.device.model.InstallationInfo
import com.maxmind.device.model.LocaleInfo
import com.maxmind.device.model.StoredID
import com.maxmind.device.storage.StoredIDStorage
import java.util.Locale
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
 */
internal class DeviceDataCollector(
    private val context: Context,
    storedIDStorage: StoredIDStorage? = null,
    private val enableLogging: Boolean = false,
) {
    private companion object {
        private const val TAG = "DeviceDataCollector"

        // Fallback values for when collection fails
        private val BUILD_INFO_FALLBACK = BuildInfo(
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

        private val DISPLAY_INFO_FALLBACK = DisplayInfo(
            widthPixels = 0,
            heightPixels = 0,
            densityDpi = 0,
            density = 0f,
        )

        private val HARDWARE_INFO_FALLBACK = HardwareInfo(
            cpuCores = 0,
            totalMemoryBytes = 0L,
            totalStorageBytes = 0L,
        )

        private val INSTALLATION_INFO_FALLBACK = InstallationInfo(
            firstInstallTime = 0L,
            lastUpdateTime = 0L,
            versionCode = 0L,
        )

        private val LOCALE_INFO_FALLBACK = LocaleInfo(
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
    private inline fun <T> collectSafe(fallback: T, block: () -> T): T =
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
            build = collectSafe(BUILD_INFO_FALLBACK) { collectBuildInfo() },
            display = collectSafe(DISPLAY_INFO_FALLBACK) { collectDisplayInfo() },
            hardware = collectSafe(HARDWARE_INFO_FALLBACK) { collectHardwareInfo() },
            gpu = gpuCollector.collect(),
            audio = audioCollector.collect(),
            sensors = sensorCollector.collect(),
            cameras = cameraCollector.collect(),
            codecs = codecCollector.collect(),
            systemFeatures = systemFeaturesCollector.collect(),
            network = networkCollector.collect(),
            installation = collectSafe(INSTALLATION_INFO_FALLBACK) { collectInstallationInfo() },
            settings = settingsCollector.collect(),
            behavior = behaviorCollector.collect(),
            telephony = telephonyCollector.collect(),
            fonts = fontCollector.collect(),
            locale = collectSafe(LOCALE_INFO_FALLBACK) { collectLocaleInfo() },
            // Timezone offset in minutes (uses getOffset to account for DST)
            timezoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60000,
            deviceTime = System.currentTimeMillis(),
            webViewUserAgent = webViewCollector.collectUserAgent(),
        )

    private fun collectBuildInfo(): BuildInfo =
        BuildInfo(
            fingerprint = Build.FINGERPRINT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            bootloader = Build.BOOTLOADER,
            osVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
        )

    private fun collectDisplayInfo(): DisplayInfo {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            ?: return DISPLAY_INFO_FALLBACK

        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            ?: return DISPLAY_INFO_FALLBACK

        val displayMetrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        display.getMetrics(displayMetrics)

        // Get refresh rate using modern API on Android R+
        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display.mode.refreshRate
        } else {
            @Suppress("DEPRECATION")
            display.refreshRate
        }
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        display.getMetrics(displayMetrics)

        // Get refresh rate using modern API on Android R+
        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display.mode.refreshRate
        } else {
            @Suppress("DEPRECATION")
            display.refreshRate
        }

        // Collect HDR capabilities on Android N+ (API 24)
        val hdrCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            display.hdrCapabilities?.supportedHdrTypes?.toList()
        } else {
            null
        }

        return DisplayInfo(
            widthPixels = displayMetrics.widthPixels,
            heightPixels = displayMetrics.heightPixels,
            densityDpi = displayMetrics.densityDpi,
            density = displayMetrics.density,
            refreshRate = refreshRate,
            hdrCapabilities = hdrCapabilities,
        )
    }

    private fun collectHardwareInfo(): HardwareInfo {
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

    private fun collectInstallationInfo(): InstallationInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        val versionCode = packageInfo.longVersionCode

        @Suppress("SwallowedException")
        val installerPackage =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } catch (e: PackageManager.NameNotFoundException) {
                    // Package not found is expected for some installation scenarios, return null
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }

        return InstallationInfo(
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime,
            installerPackage = installerPackage,
            versionCode = versionCode,
            versionName = packageInfo.versionName,
        )
    }

    private fun collectLocaleInfo(): LocaleInfo {
        val locale = Locale.getDefault()
        val timezone = TimeZone.getDefault()

        return LocaleInfo(
            language = locale.language,
            country = locale.country,
            timezone = timezone.id,
        )
    }
}
