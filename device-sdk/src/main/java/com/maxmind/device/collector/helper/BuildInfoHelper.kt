package com.maxmind.device.collector.helper

import android.os.Build
import com.maxmind.device.model.BuildInfo

/**
 * Helper class for collecting device build information.
 *
 * Encapsulates access to [Build] static fields for testability.
 */
internal class BuildInfoHelper {
    /**
     * Collects build information from the device.
     *
     * @return [BuildInfo] containing device build details
     */
    public fun collect(): BuildInfo =
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
}
