

package usecases

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechStreamService
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import javax.inject.Inject


class TranscribeAudioUseCase @Inject constructor() {

    companion object {
        private const val TAG = "TranscribeUseCase"
    }

    fun transcribeAudio(context: Context, aacAudioFile: File): String{

        Log.d(TAG, "Starting transcription for AAC file: ${aacAudioFile.absolutePath}, size: ${aacAudioFile.length()}")
        saveAacFile(context, aacAudioFile, "AAACAACAAC")
        //конвертим аас который извелкли из видео в wav
        val wavAudioFile = convertAacToWav(aacAudioFile)
        Log.d(TAG, "WAV file created: ${wavAudioFile.absolutePath}, size: ${wavAudioFile.length()}")
        saveWavFile(context, wavAudioFile, "WAVWAVWA" )
        //достаём создаём модель ии

        return extractAudioFromWav(wavAudioFile, context)


    }

    fun extractAudioFromWav(wavFile: File, context: Context): String{
        Log.d(TAG, "Starting extractAudioFromWav for: ${wavFile.absolutePath}, size: ${wavFile.length()}")
        LibVosk.setLogLevel(LogLevel.INFO)
        val modelName = "model-small-ru"
        val modelDir = File(context.filesDir, modelName)
        if (!modelDir.exists()) {
            Log.d(TAG, "Model directory does not exist, copying from assets: $modelName")
            copyAssetFolder(context.assets, modelName, modelDir)
            Log.d(TAG, "Model copied to: ${modelDir.absolutePath}")
        } else {
            Log.d(TAG, "Model directory already exists: ${modelDir.absolutePath}")
        }
        val modelPath = modelDir.absolutePath

        var model: Model? = null
        var recognizer: Recognizer? = null
        var bis: BufferedInputStream? = null

        try {

            Log.d(TAG, "Creating model from: $modelPath")
            model = Model(modelPath)
            Log.d(TAG, "Model created successfully")

            Log.d(TAG, "Creating recognizer with sample rate 16000.0f")
            recognizer = Recognizer(model, 16000.0f)
            Log.d(TAG, "Recognizer created successfully")

            Log.d(TAG, "Creating input stream from WAV file")
            bis = BufferedInputStream(FileInputStream(wavFile))
            Log.d(TAG, "Input stream created")

            Log.d(TAG, "Skipping 44 bytes of WAV header")
            bis.skip(44)
            Log.d(TAG, "Header skipped")

            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead = 0L

            Log.d(TAG, "Starting to read WAV data")
            while (bis.read(buffer).also { bytesRead = it } != -1){
                if (bytesRead > 0){
                    Log.d(TAG, "Read $bytesRead bytes from WAV, total so far: ${totalBytesRead + bytesRead}")
                    totalBytesRead += bytesRead
                    recognizer.acceptWaveForm(buffer, bytesRead)
                }
            }
            Log.d(TAG, "Finished reading WAV data, total bytes processed: $totalBytesRead")

            Log.d(TAG, "Getting final result from recognizer")
            val finalResult = recognizer.finalResult
            Log.d(TAG, "Raw final result: $finalResult")

            val resultJson = JSONObject(finalResult)
            Log.d(TAG, "Parsed JSON: $resultJson")
            val text = resultJson.getString("text")

            Log.d(TAG, "Extracted text: '$text'")


            return text.trim()
        }
        catch (e: Exception){
            Log.e(TAG, "Exception during transcription", e)
            return ""
        }
        finally {
            recognizer?.close()
            model?.close()
            bis?.close()
            Log.d(TAG, "Closed resources")
        }

    }


    fun isWavFile(file: File): Boolean {
        // Check extension first
        if (!file.extension.equals("wav", ignoreCase = true)) return false

        // Check WAV magic bytes: RIFF....WAVE
        return try {
            RandomAccessFile(file, "r").use { raf ->
                val buffer = ByteArray(12)
                raf.readFully(buffer)
                val riff = String(buffer, 0, 4).equals("RIFF", ignoreCase = true)
                val wave = String(buffer, 8, 4).equals("WAVE", ignoreCase = true)
                riff && wave
            }
        } catch (e: Exception) {
            false
        }
    }

    //test
    fun saveAacFile(context: Context, inputFile: File, fileName: String): String? {
        try {
            // Получаем путь к внутренней директории приложения
            val outputDir = context.getExternalFilesDir(null) // или context.filesDir для внутренней памяти
            val outputFile = File(outputDir, "$fileName.aac")

            // Копируем содержимое входного файла в новый файл
            FileInputStream(inputFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

            Log.d(TAG, "Saved AAC file to: ${outputFile.absolutePath}")
            return outputFile.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving AAC file", e)
            e.printStackTrace()
            return null
        }
    }

    //test
    fun saveWavFile(context: Context, inputFile: File, fileName: String): String? {
        try {
            // Получаем путь к внутренней директории приложения
            val outputDir = context.getExternalFilesDir(null) // или context.filesDir для внутренней памяти
            val outputFile = File(outputDir, "$fileName.wav")

            // Копируем содержимое входного файла в новый файл
            FileInputStream(inputFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

            Log.d(TAG, "Saved WAV file to: ${outputFile.absolutePath}")
            return outputFile.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving WAV file", e)
            e.printStackTrace()
            return null
        }
    }

    fun convertAacToWav(audioFile: File): File {
        Log.d(TAG, "Starting conversion of AAC: ${audioFile.absolutePath} to WAV")
        val wavFile = File.createTempFile("converted", ".wav", audioFile.parentFile)
        val extractor = MediaExtractor().apply { setDataSource(audioFile.absolutePath) }
        val format = extractor.getTrackFormat(0)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("Not AAC")
        if (!mime.startsWith("audio/")) throw IllegalArgumentException("Not audio")
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        Log.d(TAG, "AAC format: mime=$mime, sampleRate=$sampleRate, channels=$channels")
        extractor.selectTrack(0)
        val decoder = MediaCodec.createDecoderByType(mime).apply {
            configure(format, null, null, 0)
            start()
        }
        val bufferInfo = MediaCodec.BufferInfo()
        val outputStream = FileOutputStream(wavFile)
        var totalPcmBytes = 0L
        while (true) {
            val inputBufferIndex = decoder.dequeueInputBuffer(10000L)
            if (inputBufferIndex < 0) break
            val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
            val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
            if (sampleSize < 0) {
                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                break
            }
            val presentationTimeUs = extractor.sampleTime
            decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0)
            extractor.advance()
            var outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000L)
            while (outputBufferIndex >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                val encodedSize = bufferInfo.size
                if (encodedSize != 0) {
                    outputBuffer!!.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + encodedSize)
                    val chunk = ByteArray(encodedSize)
                    outputBuffer.get(chunk)
                    outputStream.write(chunk)
                    totalPcmBytes += encodedSize
                }
                decoder.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0L)
            }
            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
        }
        // Drain remaining output
        while (true) {
            val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000L)
            if (outputBufferIndex < 0) break
            if (outputBufferIndex >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                val encodedSize = bufferInfo.size
                if (encodedSize != 0) {
                    outputBuffer!!.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + encodedSize)
                    val chunk = ByteArray(encodedSize)
                    outputBuffer.get(chunk)
                    outputStream.write(chunk)
                    totalPcmBytes += encodedSize
                }
                decoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
        decoder.stop()
        decoder.release()
        extractor.release()
        outputStream.close()

        Log.d(TAG, "Resampling PCM from 48kHz to 16kHz")
        val pcmData = wavFile.readBytes() // Читаем raw PCM из wavFile
        val resampledPcm = resamplePcmTo16kHz(pcmData = pcmData, originalSampleRate = 48000, targetSampleRate = 16000, channels = channels)
        totalPcmBytes = resampledPcm.size.toLong()
// Затем перезаписать wavFile с resampled данными
        FileOutputStream(wavFile).use { it.write(resampledPcm) }

        Log.d(TAG, "Raw PCM data written: $totalPcmBytes bytes")

        // Write WAV header prepended to data
        val byteRate = sampleRate * channels * 2 // 16-bit
        val blockAlign = channels * 2
        val dataSize = totalPcmBytes
        val riffSize = (dataSize + 36).toInt()

        val header = ByteArray(44).apply {
            // RIFF
            set(0, 'R'.code.toByte()); set(1, 'I'.code.toByte()); set(2, 'F'.code.toByte()); set(3, 'F'.code.toByte())
            // File size - 8
            putIntLE(4, riffSize)
            // WAVE
            set(8, 'W'.code.toByte()); set(9, 'A'.code.toByte()); set(10, 'V'.code.toByte()); set(11, 'E'.code.toByte())
            // fmt
            set(12, 'f'.code.toByte()); set(13, 'm'.code.toByte()); set(14, 't'.code.toByte()); set(15, ' '.code.toByte())
            // Subchunk1Size
            putIntLE(16, 16)
            // AudioFormat (1=PCM)
            putShortLE(20, 1)
            // NumChannels
            putShortLE(22, channels.toShort())
            // SampleRate
            putIntLE(24, sampleRate)
            // ByteRate
            putIntLE(28, byteRate)
            // BlockAlign
            putShortLE(32, blockAlign.toShort())
            // BitsPerSample
            putShortLE(34, 16)
            // data
            set(36, 'd'.code.toByte()); set(37, 'a'.code.toByte()); set(38, 't'.code.toByte()); set(39, 'a'.code.toByte())
            // Subchunk2Size
            putIntLE(40, dataSize.toInt())
        }

        val finalWav = File.createTempFile("final", ".wav", audioFile.parentFile)
        FileOutputStream(finalWav).use { fos ->
            fos.write(header)
            FileInputStream(wavFile).use { fis ->
                fis.copyTo(fos)
            }
        }
        wavFile.delete()

        Log.d(TAG, "Final WAV created: ${finalWav.absolutePath}, sampleRate=$sampleRate, channels=$channels, dataSize=$dataSize")
        if (sampleRate != 16000) {
            Log.w(TAG, "Warning: WAV sample rate ($sampleRate) does not match recognizer (16000)")
        }

        return finalWav
    }

    private fun ByteArray.putIntLE(offset: Int, value: Int) {
        this[offset] = (value % 256).toByte()
        this[offset + 1] = ((value / 256) % 256).toByte()
        this[offset + 2] = ((value / 65536) % 256).toByte()
        this[offset + 3] = ((value / 16777216) % 256).toByte()
    }

    private fun resamplePcmTo16kHz(pcmData: ByteArray, originalSampleRate: Int, targetSampleRate: Int, channels: Int): ByteArray {
        val ratio = originalSampleRate.toFloat() / targetSampleRate.toFloat() // 3.0f
        val numSamples = pcmData.size / (channels * 2) // 16-bit
        val newNumSamples = (numSamples / ratio).toInt()
        val resampled = ByteArray(newNumSamples * channels * 2)

        // Простой downsampling: берём каждый 3-й сэмпл (для точности — average, но для теста ок)
        var newIdx = 0
        for (i in 0 until numSamples step (ratio.toInt())) {
            if (newIdx < newNumSamples) {
                val srcOffset = i * channels * 2
                val dstOffset = newIdx * channels * 2
                System.arraycopy(pcmData, srcOffset, resampled, dstOffset, channels * 2)
                newIdx++
            }
        }
        Log.d(TAG, "Resampled from ${pcmData.size} to ${resampled.size} bytes")
        return resampled
    }

    private fun ByteArray.putShortLE(offset: Int, value: Short) {
        this[offset] = (value % 256).toByte()
        this[offset + 1] = ((value / 256) % 256).toByte()
    }

    private fun copyAssetFolder(
        assetManager: android.content.res.AssetManager,
        fromAssetPath: String,
        toPath: File
    ) {
        if (!toPath.exists()) {
            toPath.mkdirs()
        }

        try {
            val files = assetManager.list(fromAssetPath) ?: return
            for (filename in files) {
                val from = if (fromAssetPath.isEmpty()) filename else "$fromAssetPath/$filename"
                val toFile = File(toPath, filename)
                val isDirectory = try {
                    val subfiles = assetManager.list(from)
                    subfiles.isNullOrEmpty().not()
                } catch (e: IOException) {
                    false
                }

                if (isDirectory) {
                    copyAssetFolder(assetManager, from, toFile)
                } else {
                    assetManager.open(from).use { inputStream ->
                        toFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error copying asset folder", e)
            e.printStackTrace()
        }
    }


}

