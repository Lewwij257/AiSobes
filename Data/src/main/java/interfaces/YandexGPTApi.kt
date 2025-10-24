package interfaces

import datamodel.AiRequest
import datamodel.AiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface YandexGPTApi {
    @POST("foundationModels/v1/completion")
    suspend fun sendInterviewData(
        @Header("Authorization") authHeader: String,
        @Body request: AiRequest
    ): AiResponse
}