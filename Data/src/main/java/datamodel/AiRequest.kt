package datamodel

import android.health.connect.datatypes.units.Temperature
import android.net.Uri


data class AiRequest(
    val modelUri: String = "gpt://aje30k1djfvl9e4a7ale/yandexgpt-lite",

    val stream: Boolean = false,
    val temperature: Float = 0.5f, //0-1 вариативность ответа
    val maxTokens: Int = 2000,

    val messages: List<AiModelMessage> = emptyList()

)

data class AiResponse(
    val result: List<Generation>? = emptyList()
)

data class AiModelMessage(
    val role: String, //system, user, assistant
    val message: String
)

data class Generation(
    val message: AiModelMessage,
    val alternatives: List<Alternative> = emptyList()
)

data class Alternative(
    val message: AiModelMessage,
    val logprobs: Any? = null,
    val finalReason: String? = null
)