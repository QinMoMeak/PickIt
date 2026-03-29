package com.pickit.app.domain.service

import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.VisionParseRequest

interface VisionParseService {
    suspend fun parseProduct(request: VisionParseRequest): Result<ParsedProductResult>
}
