package com.pickit.app.infrastructure.ai.error

open class VisionModelException(message: String, cause: Throwable? = null) : Exception(message, cause)

class ProviderConfigurationException(message: String) : VisionModelException(message)

class MediaAccessException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class NetworkRequestException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class ProviderHttpException(
    val statusCode: Int,
    message: String,
) : VisionModelException(message)

class ModelNonJsonResponseException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class ModelJsonParseException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)
