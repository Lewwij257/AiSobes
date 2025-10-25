package datamodel

data class AiRequest(
    // ↓ Модель теперь обязательный параметр [citation:10]
    val model: String,
    val messages: List<AiModelMessage> = emptyList(),
    val temperature: Float = 0.5f,
    val maxTokens: Int = 2000,
    val stream: Boolean = false
)



// Остальные data class остаются без изменений
data class AiModelMessage(
    val role: String,
    val content: String
)

data class AiResponse(
    val choices: List<Choice> = emptyList(),
    val error: ErrorDetail? = null // Добавьте на случай ошибок
)

data class ErrorDetail(
    val message: String,
    val code: Int
)

data class Choice(
    val message: AiModelMessage
)