package usecases

import entities.InterviewInfo
import repositories.AiRepositoryImpl
import javax.inject.Inject

class AnalyzeInterviewUseCase @Inject constructor(
    private val aiRepositoryImpl: AiRepositoryImpl
) {
    suspend fun analyze(interviewInfo: InterviewInfo): String {
        return aiRepositoryImpl.sendInterviewDataToAi(interviewInfo)
    }
}