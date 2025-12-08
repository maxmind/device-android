package com.maxmind.device.collector

import android.content.Context
import android.telephony.TelephonyManager
import com.maxmind.device.model.TelephonyInfo

/**
 * Collects telephony context information.
 *
 * Collects basic telephony information that doesn't require runtime permissions.
 */
internal class TelephonyCollector(
    private val context: Context,
) {
    /**
     * Collects current telephony information.
     *
     * @return [TelephonyInfo] containing telephony context, or null if unavailable
     */
    @Suppress("SwallowedException")
    fun collect(): TelephonyInfo? {
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                    ?: return null

            TelephonyInfo(
                networkOperatorName = telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() },
                simState = telephonyManager.simState,
                phoneType = telephonyManager.phoneType,
                hasIccCard = telephonyManager.hasIccCard(),
            )
        } catch (
            e: SecurityException,
        ) {
            // Telephony info may fail on some devices due to permission issues
            null
        }
    }
}
