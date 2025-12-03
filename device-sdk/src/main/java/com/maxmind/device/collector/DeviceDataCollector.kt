package com.maxmind.device.collector

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.Display
import com.maxmind.device.model.BuildInfo
import com.maxmind.device.model.DeviceData
import com.maxmind.device.model.DisplayInfo
import com.maxmind.device.model.HardwareInfo
import com.maxmind.device.model.InstallationInfo
import com.maxmind.device.model.LocaleInfo
import java.util.Locale
import java.util.TimeZone

/**
 * Collects device information from the Android system.
 *
 * This class is responsible for gathering various device attributes
 * that are available through the Android APIs.
 */
internal class DeviceDataCollector(private val context: Context) {
    /**
     * Collects current device data.
     *
     * @return [DeviceData] containing collected device information
     */
    fun collect(): DeviceData {
        return DeviceData(
            build = collectBuildInfo(),
            display = collectDisplayInfo(),
            hardware = collectHardwareInfo(),
            installation = collectInstallationInfo(),
            locale = collectLocaleInfo(),
            // Timezone offset in minutes
            timezoneOffset = TimeZone.getDefault().rawOffset / 60000,
            deviceTime = System.currentTimeMillis(),
            // Other fields will be populated by dedicated collectors in future commits
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
