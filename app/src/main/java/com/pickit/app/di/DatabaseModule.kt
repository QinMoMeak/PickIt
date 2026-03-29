package com.pickit.app.di

import android.content.Context
import androidx.room.Room
import com.pickit.app.data.local.dao.ParseLogDao
import com.pickit.app.data.local.dao.PriceHistoryDao
import com.pickit.app.data.local.dao.ProductDao
import com.pickit.app.data.local.dao.TagDao
import com.pickit.app.data.local.db.PickItDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PickItDatabase = Room.databaseBuilder(
        context,
        PickItDatabase::class.java,
        "pickit.db",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideProductDao(database: PickItDatabase): ProductDao = database.productDao()

    @Provides
    fun provideTagDao(database: PickItDatabase): TagDao = database.tagDao()

    @Provides
    fun providePriceHistoryDao(database: PickItDatabase): PriceHistoryDao = database.priceHistoryDao()

    @Provides
    fun provideParseLogDao(database: PickItDatabase): ParseLogDao = database.parseLogDao()
}
