package usecases

import entities.InterviewInfo
import javax.inject.Inject

class StartInterviewUseCase @Inject constructor(){

    fun start(): InterviewInfo{

        //TODO: ЗДЕСЬ БРАТЬ ИНТЕРВЬЮ ИЗ СЕТИ, АЙДИ И ВОПРОСЫ И ВООБЩЕ ВСЁ, ЭТО ПРОСТО ЗАТЫЧКА


        val id = System.currentTimeMillis().toString()
        val questions = listOf<String>("сколько вам лет?", "Вы работаете в данный момент?", "Кем видите себя через 5 лет?")

        return InterviewInfo(id = id, questions = questions)
    }

}