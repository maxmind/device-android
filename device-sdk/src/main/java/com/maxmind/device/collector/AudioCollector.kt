package com.maxmind.device.collector

import android.content.Context
import android.media.AudioManager
import com.maxmind.device.model.AudioInfo

/**
 * Collects audio hardware profile information.
 *
 * Uses AudioManager to query the device's native audio capabilities.
 */
internal class AudioCollector(
    private val context: Context,
) {
    /**
     * Collects audio hardware information.
     *
     * @return [AudioInfo] containing audio profile details
     */
    fun collect(): AudioInfo {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return AudioInfo()

        val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val framesPerBuffer =
            audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)

        return AudioInfo(
            outputSampleRate = sampleRate,
            outputFramesPerBuffer = framesPerBuffer,
        )
    }
}
