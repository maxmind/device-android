package com.maxmind.device.collector.helper

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.maxmind.device.model.InstallationInfo

/**
 * Helper class for collecting app installation information.
 *
 * Encapsulates access to [PackageManager] for testability.
 */
internal class InstallationInfoHelper(
    private val context: Context,
) {
    /**
     * Collects installation information for the current app.
     *
     * @return [InstallationInfo] containing install times, version info, and installer details
     */
    @Suppress("SwallowedException", "DEPRECATION")
    public fun collect(): InstallationInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        val versionCode = packageInfo.longVersionCode

        val installerPackage =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } catch (e: PackageManager.NameNotFoundException) {
                    // Package not found is expected for some installation scenarios, return null
                    null
                }
            } else {
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
}
