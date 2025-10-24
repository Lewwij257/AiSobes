package usecases

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ExtractAudioUseCase @Inject constructor() {

    private val TAG = "ExtractAudioUseCase"

    @RequiresApi(Build.VERSION_CODES.Q)
    fun extractAudio(context: Context, videoFile: File): File? {
        // Log input video file details
        val fileSizeBytes = videoFile.length()
        val fileSizeMB = fileSizeBytes / (1024.0 * 1024.0)
        Log.d(TAG, "Input video file: ${videoFile.absolutePath}")
        Log.d(TAG, "Input video file size: ${fileSizeBytes} bytes (${String.format("%.2f", fileSizeMB)} MB)")

        val outputAudioFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "extracted_audio_${System.currentTimeMillis()}.aac"
        )

        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        var pfd: ParcelFileDescriptor? = null
        var audioTrackIndex = -1

        try {
            extractor = MediaExtractor()

            // Use ParcelFileDescriptor to open the file for reliable track detection
            pfd = ParcelFileDescriptor.open(videoFile, ParcelFileDescriptor.MODE_READ_ONLY)
            extractor.setDataSource(pfd.fileDescriptor)

            // Log video duration if available (from any track, usually consistent)
            var videoDurationUs = -1L
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                videoDurationUs = format.getLong(MediaFormat.KEY_DURATION, -1L)
                if (videoDurationUs > 0) {
                    val videoDurationMs = videoDurationUs / 1000
                    Log.d(TAG, "Video duration: ${videoDurationMs} ms (${videoDurationMs / 1000} seconds)")
                    break
                }
            }

            // Debug: Log total tracks
            Log.d(TAG, "Total tracks in video file: ${extractor.trackCount}")

            var audioFormat: MediaFormat? = null
            var audioTrackId = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                Log.d(TAG, "Track $i: mime = $mime") // Debug: Log each track's MIME

                if (mime?.startsWith("audio/") == true) {
                    audioFormat = format
                    audioTrackId = i
                    extractor.selectTrack(i)
                    Log.d(TAG, "Selected audio track $i with format: $audioFormat")
                    // Log additional audio track details
                    val audioSampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE, -1)
                    val audioChannels = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT, -1)
                    val audioBitrate = audioFormat.getInteger(MediaFormat.KEY_BIT_RATE, -1)
                    Log.d(TAG, "Audio track details - Sample rate: $audioSampleRate Hz, Channels: $audioChannels, Bitrate: $audioBitrate bps")
                    break
                } else if (mime?.startsWith("video/") == true) {
                    // Log video track details for completeness
                    val videoWidth = format.getInteger(MediaFormat.KEY_WIDTH, -1)
                    val videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT, -1)
                    val videoBitrate = format.getInteger(MediaFormat.KEY_BIT_RATE, -1)
                    Log.d(TAG, "Video track $i details - Resolution: ${videoWidth}x${videoHeight}, Bitrate: $videoBitrate bps")
                }
            }

            if (audioFormat == null) {
                Log.e(TAG, "No audio track found in video file (checked ${extractor.trackCount} tracks)")
                return null
            }

            muxer = MediaMuxer(outputAudioFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            audioTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val bufferInfo = android.media.MediaCodec.BufferInfo()
            val buffer = ByteArray(1024 * 1024) // 1MB buffer

            var sampleCount = 0
            while (true) {
                val sampleSize = extractor.readSampleData(java.nio.ByteBuffer.wrap(buffer), 0)
                if (sampleSize < 0) break

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.flags = if (extractor.sampleFlags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME != 0) {
                    android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    0
                }
                bufferInfo.presentationTimeUs = extractor.sampleTime

                muxer.writeSampleData(audioTrackIndex, java.nio.ByteBuffer.wrap(buffer, 0, sampleSize), bufferInfo)
                extractor.advance()
                sampleCount++
            }

            muxer.stop()
            Log.d(TAG, "Audio extracted successfully to ${outputAudioFile.absolutePath} ($sampleCount samples)")
            Log.d("GlobalDebug", "Audio extracted")
            return outputAudioFile

        } catch (e: IOException) {
            Log.e(TAG, "Error extracting audio: ${e.message}", e)
            return null
        } finally {
            try {
                muxer?.release()
                extractor?.release()
                pfd?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing resources: ${e.message}", e)
            }
        }
    }
}