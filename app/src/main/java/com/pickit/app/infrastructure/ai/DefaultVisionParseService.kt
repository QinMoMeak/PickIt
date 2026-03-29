package com.pickit.app.infrastructure.ai

import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.VisionParseRequest
import com.pickit.app.domain.service.VisionParseService
import com.pickit.app.infrastructure.ai.config.ModelProviderConfigResolver
import com.pickit.app.infrastructure.ai.factory.ModelProviderFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultVisionParseService @Inject constructor(
    private val configResolver: ModelProviderConfigResolver,
    private val providerFactory: ModelProviderFactory,
) : VisionParseService {
    override suspend fun parseProduct(request: VisionParseRequest): Result<ParsedProductResult> {
        val config = runCatching { configResolver.resolve() }
        if (config.isFailure) {
            return Result.failure(config.exceptionOrNull()!!)
        }

        val providerClient = providerFactory.create(config.getOrThrow().provider)
        return providerClient.parse(config.getOrThrow(), request)
    }
}
