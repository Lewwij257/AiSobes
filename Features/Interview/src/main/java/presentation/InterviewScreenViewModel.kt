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
        //startVideoPreview(lifecycleOwner, previewView)
    }


    fun nextQuestion() {
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
    fun stopRecording(){

        viewModelScope.launch {

            Log.d("InterviewScreenViewModel", "0")
            _uiState.value = _uiState.value.copy(isRecording = false)
            Log.d("InterviewScreenViewModel", "1")
            val recordedVideo = recordVideoUseCase.stopCameraRecording()
            Log.d("InterviewScreenViewModel", "2")
            if (recordedVideo!=null){
                viewModelScope.launch {
                    Log.d("InterviewScreenViewModel", "3")
                    val audioFile = extractAudioUseCase.extractAudio(context, recordedVideo)?: return@launch
                    val textFromVideoFile = transcribeAudioUseCase.transcribeAudio(context, audioFile)
                    Log.d("InterviewScreenViewModel", textFromVideoFile.toString())
                }

            }
            else{
                Log.d("InterviewScreenViewModel", "interview is null")

            }
        }

    }

    fun stopVideoPreview() {
        //recordVideoUseCase.stopCameraPreview()
    }


    fun sendInterviewToAi(interviewInfo: InterviewInfo){
        viewModelScope.launch {
            val answer = analyzeInterviewUseCase.analyze(interviewInfo)
            Log.d("ответ: ", answer.toString())
        }
    }
}