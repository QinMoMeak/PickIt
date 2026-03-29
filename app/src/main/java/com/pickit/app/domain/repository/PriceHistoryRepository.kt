package com.pickit.app.domain.repository

import com.pickit.app.domain.model.PriceHistory

interface PriceHistoryRepository {
    suspend fun addPriceHistory(history: PriceHistory)
    suspend fun getPriceHistory(productId: String): List<PriceHistory>
}
