package repositories

import android.util.Log
import com.google.gson.Gson
import datamodel.AiModelMessage
import datamodel.AiRequest
import entities.InterviewInfo
import interfaces.AiRepository
import interfaces.OpenRouterApi // ← Изменил импорт
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class AiRepositoryImpl @Inject constructor() : AiRepository {

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("ApiLog", message)
        }
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // ↓ Базовый URL изменен на OpenRouter
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val gson = Gson()

    // ↓ Замени на свой API-ключ с OpenRouter (бесплатный на openrouter.ai/settings/keys)
    private val apiKey = "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"

    private val api: OpenRouterApi by lazy { retrofit.create(OpenRouterApi::class.java) } // ← Новый интерфейс

    override suspend fun sendInterviewDataToAi(interviewInfo: InterviewInfo): String {
        return withContext(Dispatchers.IO) {
            try {
                val interviewJson = gson.toJson(interviewInfo)
//                val prompt = "Вот данные о собеседовании в формате JSON: $interviewJson. " +
//                        "Проанализируй эту информацию. В ответе дай ТОЛЬКО ЧИСЛО - количество баллов от 0 до 100 которое по твоему мнению набрал пользователь. " +
//                        "кандидат подходит если он в возрасте от 18 до 24 и если он нигде не работает, вопрос кем он видит себя через 5 лет не играет роли. " +
//                        "ВАЖНО: Твой ответ должен быть ТОЛЬКО ОДНИМ ЧИСЛОМ (например, '85'). НИКАКИХ ОБЪЯСНЕНИЙ, ТЕКСТА, ПУНКТОВ ИЛИ СЛОВ! Если ответ не число — это ошибка. ПОВТОРЯЮ: ТОЛЬКО ЧИСЛО!"

                val prompt = "Вот данные о собеседовании в формате JSON: $interviewJson. " +
                        "Проанализируй эту информацию. В ответе дай ТОЛЬКО ЧИСЛО - количество баллов от 0 до 100 которое по твоему мнению набрал пользователь. " +
                        "кандидат подходит если он в возрасте от 18 до 24 и если он нигде не работает, вопрос кем он видит себя через 5 лет не играет роли" +
                        "Повторю, в ответе укажи ТОЛЬКО КОЛИЧЕСТВО БАЛЛОВ которые набрал пользователь одним числом. Твой ответ должен представлять из себя ТОЛЬКО ЧИСЛО! БЕЗ ТЕКСТА!!!"


                //val prompt = "где обитают львы?"

                val request = AiRequest(
                    // ↓ Указываем конкретную модель OpenRouter [citation:1]
                    model = "tngtech/deepseek-r1t-chimera:free",
                    messages = listOf(
                        AiModelMessage(
                            role = "user",
                            content = prompt
                        )
                    ),
                    //temperature = 0.5f,
                    temperature = 0f,
                    //maxTokens = 2000,
                    maxTokens = 5,
                    stream = false
                )

                Log.d("ApiLog", "Запрос JSON: ${gson.toJson(request)}")

                // ↓ Формат авторизации для OpenRouter [citation:10]
                val authHeader = "Bearer $apiKey"

                Log.d("ApiLog", "Auth: $authHeader")

                val response = api.sendInterviewData(authHeader, request)
                // Обработка ответа остается прежней
                val responseText = response.choices.firstOrNull()?.message?.content ?: "no answer"

                //test:
                Log.d("apilog", responseText)

//                val score = responseText.trim().substringAfterLast(" ").toIntOrNull() ?: 0
//                score.toString()
                val score = responseText.lines().lastOrNull()
                score.toString()

            } catch (e: Exception) {
                Log.e("AiRepositoryImpl", "Error sending data: ${e.message}", e)
                "error occurred: ${e.message}"
            }
        }
    }
}