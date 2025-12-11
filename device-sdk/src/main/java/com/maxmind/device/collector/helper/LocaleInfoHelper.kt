package com.maxmind.device.collector.helper

import com.maxmind.device.model.LocaleInfo
import java.util.Locale
import java.util.TimeZone

/**
 * Helper class for collecting locale information.
 *
 * Encapsulates access to [Locale] and [TimeZone] for testability.
 */
internal class LocaleInfoHelper {
    /**
     * Collects locale information from the device.
     *
     * @return [LocaleInfo] containing language, country, and timezone
     */
    public fun collect(): LocaleInfo {
        val locale = Locale.getDefault()
        val timezone = TimeZone.getDefault()

        return LocaleInfo(
            language = locale.language,
            country = locale.country,
            timezone = timezone.id,
        )
    }
}
