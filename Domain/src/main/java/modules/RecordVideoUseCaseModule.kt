package modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import usecases.RecordVideoUseCase


@Module
@InstallIn(SingletonComponent::class)
object RecordVideoUseCaseModule {

    @Provides
    fun provideRecordVideoUseCase(): RecordVideoUseCase{
        return RecordVideoUseCase()
    }

}