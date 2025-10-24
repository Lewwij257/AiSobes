package interfaces

import entities.InterviewInfo

interface AiRepository {

    suspend fun sendInterviewDataToAi(interviewInfo: InterviewInfo): String

}