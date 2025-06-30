package com.example.audiorecordingapp.di

import android.content.Context
import androidx.room.Room
import com.example.audiorecordingapp.data.local.AppDatabase
import com.example.audiorecordingapp.data.local.dao.RecordingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext ctx: Context
    ): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRecordingDao(db: AppDatabase): RecordingDao =
        db.recordingDao()

}


