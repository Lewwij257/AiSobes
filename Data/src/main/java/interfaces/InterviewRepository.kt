package interfaces

import entities.InterviewInfo

interface InterviewRepository {

    suspend fun startInterview(interview: InterviewInfo)

    suspend fun saveInterview(interview: InterviewInfo): Result<Unit>

    suspend fun getInterview(interviewId: String): Result<InterviewInfo>

}