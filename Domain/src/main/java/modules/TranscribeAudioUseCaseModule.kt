package modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import usecases.TranscribeAudioUseCase

@Module
@InstallIn(SingletonComponent::class)
object TranscribeAudioUseCaseModule {
    @Provides
    fun provideTranscribeAudioUseCase(): TranscribeAudioUseCase{
        return TranscribeAudioUseCase()
    }
}