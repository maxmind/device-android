package com.maxmind.device.collector

import android.content.Context
import android.media.AudioManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AudioCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var mockAudioManager: AudioManager
    private lateinit var collector: AudioCollector

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockAudioManager = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.AUDIO_SERVICE) } returns mockAudioManager
        collector = AudioCollector(mockContext)
    }

    @Test
    internal fun `collect returns AudioInfo with sample rate and frames per buffer`() {
        every { mockAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) } returns "48000"
        every {
            mockAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        } returns "256"

        val result = collector.collect()

        assertNotNull(result)
        assertEquals("48000", result.outputSampleRate)
        assertEquals("256", result.outputFramesPerBuffer)
    }

    @Test
    internal fun `collect returns AudioInfo with null values when properties unavailable`() {
        every { mockAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) } returns null
        every {
            mockAudioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        } returns null

        val result = collector.collect()

        assertNotNull(result)
        assertNull(result.outputSampleRate)
        assertNull(result.outputFramesPerBuffer)
    }

    @Test
    internal fun `collect returns empty AudioInfo when AudioManager unavailable`() {
        every { mockContext.getSystemService(Context.AUDIO_SERVICE) } returns null
        val collectorWithNoAudioManager = AudioCollector(mockContext)

        val result = collectorWithNoAudioManager.collect()

        assertNotNull(result)
        assertNull(result.outputSampleRate)
        assertNull(result.outputFramesPerBuffer)
    }
}
