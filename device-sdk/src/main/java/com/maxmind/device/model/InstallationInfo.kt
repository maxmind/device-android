package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * App installation metadata.
 */
@Serializable
public data class InstallationInfo(
    @SerialName("first_install_time")
    val firstInstallTime: Long,
    @SerialName("last_update_time")
    val lastUpdateTime: Long,
    @SerialName("installer_package")
    val installerPackage: String? = null,
    @SerialName("version_code")
    val versionCode: Long,
    @SerialName("version_name")
    val versionName: String? = null,
)
