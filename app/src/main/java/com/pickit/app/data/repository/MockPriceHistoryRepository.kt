package com.pickit.app.data.repository

import com.pickit.app.core.util.SampleData
import com.pickit.app.domain.model.PriceHistory
import com.pickit.app.domain.repository.PriceHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class MockPriceHistoryRepository @Inject constructor() : PriceHistoryRepository {
    private val mutex = Mutex()
    private val history = SampleData.products.flatMap { it.priceHistory }.toMutableList()

    override suspend fun addPriceHistory(history: PriceHistory) {
        mutex.withLock {
            this.history += history
        }
    }

    override suspend fun getPriceHistory(productId: String): List<PriceHistory> = mutex.withLock {
        history.filter { it.productId == productId }.sortedByDescending { it.recordedAt }
    }
}
