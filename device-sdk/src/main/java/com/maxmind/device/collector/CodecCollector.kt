package com.maxmind.device.collector

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import com.maxmind.device.model.CodecDetail
import com.maxmind.device.model.CodecInfo

/**
 * Collects information about available media codecs.
 *
 * Enumerates all audio and video codecs available on the device
 * using MediaCodecList.
 */
internal class CodecCollector {
    /**
     * Collects information about all available codecs.
     *
     * @return [CodecInfo] containing audio and video codec lists
     */
    fun collect(): CodecInfo {
        return try {
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
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // MediaCodecList may fail on some devices
            CodecInfo()
        }
    }

    private fun isAudioCodec(codecInfo: MediaCodecInfo): Boolean {
        return codecInfo.supportedTypes.any { it.startsWith("audio/") }
    }

    private fun isVideoCodec(codecInfo: MediaCodecInfo): Boolean {
        return codecInfo.supportedTypes.any { it.startsWith("video/") }
    }
}
