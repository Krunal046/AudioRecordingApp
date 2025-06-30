package com.example.audiorecordingapp.data.local.repository

import com.example.audiorecordingapp.data.local.dao.RecordingDao
import com.example.audiorecordingapp.data.local.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val recordingDao: RecordingDao
) {

    suspend fun insertRecording(recording: RecordingEntity): Long =
        recordingDao.insert(recording)

    fun getAllRecordings(): Flow<List<RecordingEntity>> =
        recordingDao.getAll()

    fun getRecordingById(id: Long): Flow<RecordingEntity?> =
        recordingDao.getById(id)

    suspend fun updateRecording(recording: RecordingEntity) =
        recordingDao.update(recording)

    suspend fun deleteRecording(recording: RecordingEntity) =
        recordingDao.delete(recording)

}

