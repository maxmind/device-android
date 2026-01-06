package com.maxmind.device.collector.helper

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.TimeZone

internal class LocaleInfoHelperTest {
    private lateinit var helper: LocaleInfoHelper
    private lateinit var originalLocale: Locale
    private lateinit var originalTimeZone: TimeZone

    @BeforeEach
    internal fun setUp() {
        // Save original values
        originalLocale = Locale.getDefault()
        originalTimeZone = TimeZone.getDefault()
        helper = LocaleInfoHelper()
    }

    @AfterEach
    internal fun tearDown() {
        // Restore original values
        Locale.setDefault(originalLocale)
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    internal fun `collect returns locale info for US English`() {
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))

        val result = helper.collect()

        assertNotNull(result)
        assertEquals("en", result.language)
        assertEquals("US", result.country)
        assertEquals("America/New_York", result.timezone)
    }

    @Test
    internal fun `collect returns locale info for German Germany`() {
        Locale.setDefault(Locale.GERMANY)
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))

        val result = helper.collect()

        assertNotNull(result)
        assertEquals("de", result.language)
        assertEquals("DE", result.country)
        assertEquals("Europe/Berlin", result.timezone)
    }

    @Test
    internal fun `collect returns locale info for Japanese Japan`() {
        Locale.setDefault(Locale.JAPAN)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))

        val result = helper.collect()

        assertNotNull(result)
        assertEquals("ja", result.language)
        assertEquals("JP", result.country)
        assertEquals("Asia/Tokyo", result.timezone)
    }

    @Test
    internal fun `collect handles UTC timezone`() {
        Locale.setDefault(Locale.UK)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val result = helper.collect()

        assertNotNull(result)
        assertEquals("UTC", result.timezone)
    }

    @Test
    internal fun `collect handles locale with only language`() {
        // Some locales don't have country specified
        Locale.setDefault(Locale("es"))
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))

        val result = helper.collect()

        assertNotNull(result)
        assertEquals("es", result.language)
        assertEquals("", result.country) // No country for language-only locale
    }
}
