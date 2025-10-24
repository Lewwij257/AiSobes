package presentation

import entities.InterviewInfo

data class InterviewScreenUiState (
    val interviewId: String = "",
    val interviewInfo: InterviewInfo = InterviewInfo(),
    val currentQuestionIndex: Int = 0,

    val isInterviewStarted: Boolean = false,
    val isInterviewFinished: Boolean = false,

    val isRecording: Boolean = false
)