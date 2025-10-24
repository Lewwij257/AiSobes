package entities

data class InterviewInfo(
    val id: String = "123",
    val questions: List<String> = listOf<String>(),
    val userResponses: List<String> = listOf<String>()
    )