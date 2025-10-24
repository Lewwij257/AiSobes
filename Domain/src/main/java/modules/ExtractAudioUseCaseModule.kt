package modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import usecases.ExtractAudioUseCase
import usecases.RecordVideoUseCase

@Module
@InstallIn(SingletonComponent::class)
object ExtractAudioUseCaseModule {

    @Provides
    fun provideExtractAudioUseCase(): ExtractAudioUseCase{
        return ExtractAudioUseCase()
    }


}