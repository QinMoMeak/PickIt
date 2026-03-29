package com.pickit.app.infrastructure.ai.error

open class VisionModelException(message: String, cause: Throwable? = null) : Exception(message, cause)

class ProviderConfigurationException(message: String) : VisionModelException(message)

class MediaAccessException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class ImageEncodingException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class NetworkRequestException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class AuthenticationFailureException(message: String) : VisionModelException(message)

class RequestFormatException(message: String) : VisionModelException(message)

class ProviderHttpException(
    val statusCode: Int,
    message: String,
) : VisionModelException(message)

class EmptyModelResponseException(message: String) : VisionModelException(message)

class ModelNonJsonResponseException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class ModelJsonParseException(message: String, cause: Throwable? = null) : VisionModelException(message, cause)

class NoProductInfoException(message: String) : VisionModelException(message)

fun Throwable.toUserReadableMessage(): String = when (this) {
    is ProviderConfigurationException -> "AI 配置无效，请检查服务商、模型和地址"
    is AuthenticationFailureException -> "鉴权失败，请检查 API Key"
    is RequestFormatException -> "AI 请求格式错误"
    is MediaAccessException, is ImageEncodingException -> "图片编码失败或格式不支持"
    is NetworkRequestException -> "AI 服务连接失败，请稍后重试"
    is EmptyModelResponseException -> "模型返回为空"
    is ModelNonJsonResponseException, is ModelJsonParseException -> "模型返回内容无法解析"
    is NoProductInfoException -> "当前图片未识别出有效商品信息"
    is ProviderHttpException -> when (statusCode) {
        400 -> "AI 请求格式错误"
        401, 403 -> "鉴权失败，请检查 API Key"
        429 -> "模型当前访问量过大，请稍后再试"
        else -> "AI 服务请求失败（HTTP $statusCode）"
    }
    else -> message ?: "识别失败，请稍后重试"
}
