package com.pickit.app.core.util

import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.PriceHistory
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.model.ProductStatus
import com.pickit.app.domain.model.SourceType
import com.pickit.app.domain.model.Tag

object SampleData {
    val products = listOf(
        ProductItem(
            id = "sample-1",
            title = "厨房分隔收纳盒",
            brand = "宅物研",
            category = "家居",
            subCategory = "收纳",
            platform = Platform.DOUYIN,
            shopName = "宅物研旗舰店",
            currentPriceText = "39.9元",
            currentPriceValue = 39.9,
            spec = "大号三件套",
            summary = "适合冰箱与抽屉整理，节省空间。",
            recommendationReason = "结构规整，适合长期复购与整理。",
            tags = listOf(Tag("tag-1", "厨房"), Tag("tag-2", "收纳"), Tag("tag-3", "待买")),
            status = ProductStatus.NOT_PURCHASED,
            sourceType = SourceType.IMAGE_WITH_TEXT,
            sourceNote = "抖音家居博主种草截图",
            confidence = 0.84f,
            aiRawJson = "{}",
            priceHistory = listOf(
                PriceHistory(
                    id = "price-1",
                    productId = "sample-1",
                    priceText = "49.9元",
                    priceValue = 49.9,
                    platform = Platform.DOUYIN,
                    shopName = "宅物研旗舰店",
                    recordedAt = "2026-03-01T09:00:00Z",
                ),
                PriceHistory(
                    id = "price-2",
                    productId = "sample-1",
                    priceText = "39.9元",
                    priceValue = 39.9,
                    platform = Platform.DOUYIN,
                    shopName = "宅物研旗舰店",
                    recordedAt = "2026-03-20T09:00:00Z",
                ),
            ),
            createdAt = "2026-03-01T09:00:00Z",
            updatedAt = "2026-03-20T09:00:00Z",
        ),
        ProductItem(
            id = "sample-2",
            title = "冻干苹果脆",
            brand = "果趣",
            category = "零食",
            subCategory = "果干",
            platform = Platform.TAOBAO,
            shopName = "果趣食品店",
            currentPriceText = "29.9元",
            currentPriceValue = 29.9,
            spec = "2袋",
            summary = "适合办公室低负担零食。",
            recommendationReason = "配料简单，适合囤货。",
            tags = listOf(Tag("tag-4", "零食"), Tag("tag-5", "办公室")),
            status = ProductStatus.IN_USE,
            sourceType = SourceType.IMAGE,
            sourceNote = "淘宝推荐页截图",
            confidence = 0.92f,
            aiRawJson = "{}",
            priceHistory = listOf(
                PriceHistory(
                    id = "price-3",
                    productId = "sample-2",
                    priceText = "32.9元",
                    priceValue = 32.9,
                    platform = Platform.TAOBAO,
                    shopName = "果趣食品店",
                    recordedAt = "2026-02-13T11:30:00Z",
                ),
                PriceHistory(
                    id = "price-4",
                    productId = "sample-2",
                    priceText = "29.9元",
                    priceValue = 29.9,
                    platform = Platform.TAOBAO,
                    shopName = "果趣食品店",
                    recordedAt = "2026-03-18T11:30:00Z",
                ),
            ),
            createdAt = "2026-02-13T11:30:00Z",
            updatedAt = "2026-03-18T11:30:00Z",
        ),
    )
}
