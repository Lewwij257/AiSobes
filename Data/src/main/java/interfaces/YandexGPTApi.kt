package interfaces

import datamodel.AiRequest
import datamodel.AiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun sendInterviewData(
        @Header("Authorization") authHeader: String,
        @Body request: AiRequest
    ): AiResponse
}