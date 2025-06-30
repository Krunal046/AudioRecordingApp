package com.example.audiorecordingapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.audiorecordingapp.data.local.dao.RecordingDao
import com.example.audiorecordingapp.data.local.entity.RecordingEntity

@Database(
    entities = [RecordingEntity::class],
    version  = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
}
