package com.maxmind.device.collector

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.util.Log
import com.maxmind.device.model.CodecDetail
import com.maxmind.device.model.CodecInfo

/**
 * Collects information about available media codecs.
 *
 * Enumerates all audio and video codecs available on the device
 * using MediaCodecList.
 */
internal class CodecCollector(
    private val enableLogging: Boolean = false,
) {
    private companion object {
        private const val TAG = "CodecCollector"
    }

    /**
     * Collects information about all available codecs.
     *
     * @return [CodecInfo] containing audio and video codec lists
     */
    fun collect(): CodecInfo =
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            val codecInfos = codecList.codecInfos

            val audioCodecs = mutableListOf<CodecDetail>()
            val videoCodecs = mutableListOf<CodecDetail>()

            for (codecInfo in codecInfos) {
                val detail =
                    CodecDetail(
                        name = codecInfo.name,
                        supportedTypes = codecInfo.supportedTypes.toList(),
                        isEncoder = codecInfo.isEncoder,
                    )

                if (isAudioCodec(codecInfo)) {
                    audioCodecs.add(detail)
                } else if (isVideoCodec(codecInfo)) {
                    videoCodecs.add(detail)
                }
            }

            CodecInfo(
                audio = audioCodecs,
                video = videoCodecs,
            )
        } catch (
            @Suppress("SwallowedException")
            e: IllegalArgumentException,
        ) {
            // MediaCodecList may fail on some devices
            if (enableLogging) {
                Log.d(TAG, "Failed to collect codec info: ${e.message}")
            }
            CodecInfo()
        } catch (
            @Suppress("SwallowedException")
            e: IllegalStateException,
        ) {
            // MediaCodecList may fail on some devices
            if (enableLogging) {
                Log.d(TAG, "Failed to collect codec info: ${e.message}")
            }
            CodecInfo()
        } catch (
            @Suppress("SwallowedException")
            e: SecurityException,
        ) {
            // MediaCodecList may fail on some devices
            if (enableLogging) {
                Log.d(TAG, "Failed to collect codec info: ${e.message}")
            }
            CodecInfo()
        }

    private fun isAudioCodec(info: MediaCodecInfo): Boolean = info.supportedTypes.any { it.startsWith("audio/") }

    private fun isVideoCodec(info: MediaCodecInfo): Boolean = info.supportedTypes.any { it.startsWith("video/") }
}
