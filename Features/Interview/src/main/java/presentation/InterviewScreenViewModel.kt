package presentation

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import entities.InterviewInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import usecases.AnalyzeInterviewUseCase
import usecases.ExtractAudioUseCase
import usecases.RecordVideoUseCase
import usecases.StartInterviewUseCase
import usecases.TranscribeAudioUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InterviewScreenViewModel @Inject constructor(
    private val recordVideoUseCase: RecordVideoUseCase,
    private val extractAudioUseCase: ExtractAudioUseCase,
    private val transcribeAudioUseCase: TranscribeAudioUseCase,
    private val analyzeInterviewUseCase: AnalyzeInterviewUseCase
) : ViewModel() {

    val _uiState = MutableStateFlow(InterviewScreenUiState())
    val uiState: StateFlow<InterviewScreenUiState> = _uiState.asStateFlow()

    private lateinit var recordedVideo: File


    private lateinit var context: Context //да неправильно но камера того требует

    init {
        loadInterview()
    }

    fun loadInterview() {
        val interviewInfo = StartInterviewUseCase().start()
        Log.d("InterviewScreenViewModel","questions: ${interviewInfo.questions}")
        //_uiState.value.copy(interviewInfo = interviewInfo)
        _uiState.update { it.copy(interviewInfo = interviewInfo) }
    }



    fun startInterview(lifecycleOwner: LifecycleOwner, previewView: PreviewView){
        _uiState.update { it.copy(isInterviewStarted = true, isInterviewFinished = false, isRecording = true) }
        startRecording()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun stopInterview(){
        viewModelScope.launch {
            _uiState.update { it.copy(isInterviewFinished = true) }
            stopVideoPreview()
//            stopRecording()
//            extractTextFromInterview(recordedVideo)
//
            sendInterviewToAi(uiState.value.interviewInfo)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun nextQuestion(){
        viewModelScope.launch {
            turnNextQuestion()
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun turnNextQuestion() {

        stopRecording()

        val userAnswerToQuestion = extractTextFromInterview(recordedVideo)
        _uiState.update { it.copy(interviewInfo = _uiState.value.interviewInfo.copy(userResponses = uiState.value.interviewInfo.userResponses + userAnswerToQuestion)) }
        startRecording()

        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex < _uiState.value.interviewInfo.questions.size - 1) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex + 1)
        } else {
            // Завершить интервью
            _uiState.value = _uiState.value.copy(isInterviewFinished = true)
            _uiState.value = _uiState.value.copy(isInterviewStarted = false)


        }

    }

    fun setContext(ctx: Context){
        context = ctx
    }

    fun startVideoPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        recordVideoUseCase.startCameraPreview(context, lifecycleOwner, previewView)
    }

    fun startRecording(){
        _uiState.value = _uiState.value.copy(isRecording = true)
        recordVideoUseCase.startCameraRecording(context)
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun stopRecording(){
        Log.d("InterviewScreenViewModel", "0")
        _uiState.value = _uiState.value.copy(isRecording = false)
        Log.d("InterviewScreenViewModel", "1")
        val video = recordVideoUseCase.stopCameraRecording()
        Log.d("InterviewScreenViewModel", "2")

        recordedVideo = video!!
    }

    fun stopVideoPreview() {
        //recordVideoUseCase.stopCameraPreview()
    }


    fun sendInterviewToAi(interviewInfo: InterviewInfo){
        viewModelScope.launch {
            val answer = analyzeInterviewUseCase.analyze(interviewInfo)
            Log.d("apilog", answer.toString())
            try {
                val score = answer.toInt()
                _uiState.update { it.copy(testFinalScore = score) }

            }
            catch (e: Exception){

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun extractTextFromInterview(video: File): String{
        val audioFile = extractAudioUseCase.extractAudio(context, video)
        val textFromVideoFile = transcribeAudioUseCase.transcribeAudio(context, audioFile!!)
        Log.d("InterviewScreenViewModel", textFromVideoFile)
        return textFromVideoFile
    }
}

