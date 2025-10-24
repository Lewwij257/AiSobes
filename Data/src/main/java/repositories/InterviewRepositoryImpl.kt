package repositories

import entities.InterviewInfo
import interfaces.InterviewRepository
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(): InterviewRepository {

    private val interviews = mutableMapOf<String, InterviewInfo>()

    override suspend fun startInterview(interview: InterviewInfo) {
        //TODO:
        interview
    }

    //TODO: сделать получение интервью в старт интервью


    override suspend fun saveInterview(interview: InterviewInfo): Result<Unit> {
        return try {
            interviews[interview.id] = interview
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInterview(id: String): Result<InterviewInfo> {
        return try {
            val interview = interviews[id] ?: return Result.failure(Exception("Not found"))
            Result.success(interview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}