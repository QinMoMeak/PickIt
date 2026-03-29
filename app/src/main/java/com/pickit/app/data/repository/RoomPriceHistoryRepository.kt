package com.pickit.app.data.repository

import com.pickit.app.data.local.dao.PriceHistoryDao
import com.pickit.app.data.local.entity.PriceHistoryEntity
import com.pickit.app.data.mapper.ProductMapper
import com.pickit.app.domain.model.PriceHistory
import com.pickit.app.domain.repository.PriceHistoryRepository
import javax.inject.Inject

class RoomPriceHistoryRepository @Inject constructor(
    private val priceHistoryDao: PriceHistoryDao,
    private val mapper: ProductMapper,
) : PriceHistoryRepository {
    override suspend fun addPriceHistory(history: PriceHistory) {
        priceHistoryDao.insert(
            PriceHistoryEntity(
                id = history.id,
                productId = history.productId,
                priceText = history.priceText,
                priceValue = history.priceValue,
                currency = history.currency,
                platform = history.platform.name,
                shopName = history.shopName,
                sourceNote = history.sourceNote,
                recordedAt = history.recordedAt,
            ),
        )
    }

    override suspend fun getPriceHistory(productId: String): List<PriceHistory> =
        priceHistoryDao.getByProductId(productId).map(mapper::toDomainPriceHistory)
}
