package usecases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject



class RecordVideoUseCase @Inject constructor() {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: androidx.camera.core.Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var currentVideoFile: File? = null // Добавляем свойство для хранения текущего видео файла
    private var recordingDeferred: CompletableDeferred<File?>? = null
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.IDLE)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    @SuppressLint("RestrictedApi")
    fun startCameraPreview(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {

        _cameraState.value = CameraState.INITIALIZING


        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider?.unbindAll()
            cameraProvider = cameraProviderFuture.get()

            // Создаем Recorder для видео
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                .build()

            // Инициализируем videoCapture
            Log.d("RecordVideoUseCase", "initializeing videocapture")
            videoCapture = VideoCapture.withOutput(recorder)

            preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture // Добавляем videoCapture в привязку
                )
            } catch (e: Exception) {
                Log.e("RecordVideoUseCase", "Failed to bind camera use cases", e)
                _cameraState.value = CameraState.ERROR("Failed to bind camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
        _cameraState.value = CameraState.READY

    }

    @SuppressLint("MissingPermission", "RestrictedApi")
    fun startCameraRecording(context: Context) {
        Log.d("RecordVideoUseCase", "startCameraRecording1")

        // Проверяем, что videoCapture инициализирован
        if (videoCapture == null) {
            Log.e("RecordVideoUseCase", "videoCapture is null - call startCameraPreview first")
            _cameraState.value = CameraState.ERROR("VideoCapture not initialized")
            return
        }

        val file = createInterviewVideoFile(context)
        currentVideoFile = file // Сохраняем ссылку на файл
        val outputOptions = FileOutputOptions.Builder(file).build()

        recordingDeferred = CompletableDeferred<File?>()

        recording = videoCapture?.output
            ?.prepareRecording(context, outputOptions)
            ?.apply { withAudioEnabled() }
            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {

                        Log.d("GlobalDebug", "Video record started")

                        Log.d("RecordVideoUseCase", "startCameraRecording2 - Recording started")
                        _cameraState.value = CameraState.RECORDING
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            Log.d("RecordVideoUseCase", "Video saved: ${file.absolutePath}")
                            recordingDeferred?.complete(file)
                        } else {
                            Log.e("RecordVideoUseCase", "Recording error: ${recordEvent.error}")
                            _cameraState.value = CameraState.ERROR("Recording failed: ${recordEvent.error}")
                            recordingDeferred?.complete(null)
                        }
                        recordingDeferred = null
                    }
                }
            }
    }

    /**
     * Suspend function to stop recording and wait for the Finalize event before returning the file.
     * This ensures the MP4 file is fully written and parsable (moov atom included).
     */
    suspend fun stopCameraRecording(): File? = withContext(Dispatchers.Main) {
        recording?.stop()
        recording = null
        val deferred = recordingDeferred
        Log.d("GlobalDebug", "Video record stopped")

        deferred?.await() ?: run {
            currentVideoFile = null
            null
        }

    }

    /**
     * Non-suspend version for compatibility; blocks until finalize (use with caution, avoid on main thread).
     */
    fun stopCameraRecordingBlocking(): File? = runBlocking {
        stopCameraRecording()
    }

    /**
     * Возвращает текущий видео файл, если запись завершена.
     * Используйте после stopCameraRecording().
     */
    fun getCurrentVideoFile(): File? = currentVideoFile

    fun createInterviewVideoFile(context: Context): File {
        val time = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())
        val storagePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "interview-$time", ".mp4", storagePath
        )
    }

    suspend fun stopCameraPreview() {
        val videoFile = stopCameraRecording()
        preview = null
        videoCapture = null
        currentVideoFile = null // Очищаем ссылку на файл
        cameraProvider?.unbindAll()
        cameraProvider = null
        _cameraState.value = CameraState.IDLE
        cameraExecutor.shutdown()
    }

    fun stopCameraPreviewBlocking() {
        runBlocking {
            stopCameraPreview()
        }
    }
}

sealed class CameraState {
    object IDLE : CameraState()
    object INITIALIZING : CameraState()
    object READY : CameraState()
    object RECORDING : CameraState()
    class ERROR(val message: String) : CameraState()
}