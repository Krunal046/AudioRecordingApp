package com.example.audiorecordingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fileName: String,
    val filePath: String,
    val createdAt: Long,
    val duration: Long,
    val fileSize: Long,
    val lastPlayedPosition: Long = 0L,
    val format: String,
)

