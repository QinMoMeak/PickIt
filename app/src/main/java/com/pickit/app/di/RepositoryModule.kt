package com.pickit.app.di

import com.pickit.app.data.repository.RemoteParseRepository
import com.pickit.app.data.repository.RoomPriceHistoryRepository
import com.pickit.app.data.repository.RoomProductRepository
import com.pickit.app.data.repository.RoomSyncRepository
import com.pickit.app.domain.repository.ParseRepository
import com.pickit.app.domain.repository.PriceHistoryRepository
import com.pickit.app.domain.repository.ProductRepository
import com.pickit.app.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: RoomProductRepository): ProductRepository

    @Binds
    @Singleton
    abstract fun bindParseRepository(impl: RemoteParseRepository): ParseRepository

    @Binds
    @Singleton
    abstract fun bindPriceHistoryRepository(impl: RoomPriceHistoryRepository): PriceHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: RoomSyncRepository): SyncRepository
}
