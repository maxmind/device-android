package com.maxmind.device.collector

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDrm
import android.provider.Settings
import android.util.Base64
import android.util.Log
import com.maxmind.device.model.DeviceIDs
import java.util.UUID

/**
 * Collects device-generated persistent identifiers.
 *
 * This collector gathers hardware-backed and app-scoped identifiers
 * that can be used for device fingerprinting. These are distinct from
 * server-generated stored IDs.
 */
internal class DeviceIDsCollector(
    private val context: Context,
    private val enableLogging: Boolean = false,
) {
    /**
     * Collects device-generated identifiers.
     *
     * @return [DeviceIDs] containing available device identifiers
     */
    fun collect(): DeviceIDs =
        DeviceIDs(
            mediaDrmID = collectMediaDrmID(),
            androidID = collectAndroidID(),
        )

    /**
     * Collects the MediaDRM device unique ID.
     *
     * This ID is hardware-backed and persists through factory resets on most devices.
     * Uses the Widevine DRM system which is available on ~98% of Android devices.
     *
     * @return Base64-encoded device ID, or null if unavailable
     */
    private fun collectMediaDrmID(): String? =
        try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            try {
                val deviceId = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                Base64.encodeToString(deviceId, Base64.NO_WRAP)
            } finally {
                mediaDrm.close()
            }
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // MediaDRM may not be available on all devices (e.g., emulators, some custom ROMs)
            if (enableLogging) {
                Log.d(TAG, "Failed to collect MediaDRM ID: ${e.message}")
            }
            null
        }

    /**
     * Collects the Android ID (SSAID).
     *
     * Since Android 8.0 (API 26), this ID is scoped to the app's signing key,
     * meaning different apps get different IDs. It persists across app reinstalls
     * but changes on factory reset.
     *
     * @return The Android ID string, or null if unavailable
     */
    @SuppressLint("HardwareIds") // Intentional for fraud detection fingerprinting
    private fun collectAndroidID(): String? =
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // Settings.Secure may throw on some custom ROMs or restricted contexts
            if (enableLogging) {
                Log.d(TAG, "Failed to collect Android ID: ${e.message}")
            }
            null
        }

    internal companion object {
        private const val TAG = "DeviceIDsCollector"

        /**
         * UUID for Widevine DRM system.
         * This is a well-known UUID that identifies the Widevine DRM provider.
         */
        @Suppress("MagicNumber")
        val WIDEVINE_UUID: UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
    }
}
