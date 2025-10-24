package repositories

import android.util.Log
import com.google.gson.Gson
import datamodel.AiModelMessage
import datamodel.AiRequest
import datamodel.AiResponse
import entities.InterviewInfo
import interfaces.AiRepository
import interfaces.YandexGPTApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor() : AiRepository {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://llm.api.cloud.yandex.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val gson = Gson()

    private val apiKey = "Api-Key AQVNwwUC2b_1S4ijqq66qP0GfHNU-4IAaOGAqHwc" // инжектить или BuildConfig.YANDEX_API_KEY

    private val api: YandexGPTApi by lazy { retrofit.create(YandexGPTApi::class.java) }

    override suspend fun sendInterviewDataToAi(interviewInfo: InterviewInfo): String {
        return withContext(Dispatchers.IO) {
            try {
                val interviewJson = gson.toJson(interviewInfo)
                val prompt = "Вот данные о собеседовании в формате JSON: $interviewJson. Проанализируй эту информацию, выдели ключевые моменты, сильные и слабые стороны, и дай рекомендации по улучшению для следующих собеседований."

                val request = AiRequest(
                    modelUri = "gpt://aje30k1djfvl9e4a7ale/yandexgpt/latest",
                    messages = listOf(
                        AiModelMessage(
                            "user",
                            prompt
                        )
                    )
                )

                val response = api.sendInterviewData(apiKey, request)

                response.result?.firstOrNull()?.message?.message ?: "no answer"
            } catch (e: Exception) {
                Log.e("AiRepositoryImpl", "Error sending data: ${e.message}", e)
                "error occurred: ${e.message}"
            }
        }
    }
}