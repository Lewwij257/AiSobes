package modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import repositories.AiRepositoryImpl
import usecases.AnalyzeInterviewUseCase
import usecases.ExtractAudioUseCase

@Module
@InstallIn(SingletonComponent::class)
object AnalyzeInterviewUseCaseModule {

    @Provides
    fun provideAnalyzeInterviewUseCase(): AnalyzeInterviewUseCase{
        return AnalyzeInterviewUseCase(
            AiRepositoryImpl()
        )
    }


}