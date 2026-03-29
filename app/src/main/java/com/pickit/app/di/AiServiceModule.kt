package com.pickit.app.di

import com.pickit.app.domain.service.VisionParseService
import com.pickit.app.infrastructure.ai.DefaultVisionParseService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiServiceModule {
    @Binds
    @Singleton
    abstract fun bindVisionParseService(impl: DefaultVisionParseService): VisionParseService
}
