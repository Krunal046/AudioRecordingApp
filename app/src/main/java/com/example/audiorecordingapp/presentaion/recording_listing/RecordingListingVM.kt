package com.example.audiorecordingapp.presentaion.recording_listing

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiorecordingapp.data.local.entity.RecordingEntity
import com.example.audiorecordingapp.data.local.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingListingVM @Inject constructor(
    private val recordingRepository: RecordingRepository
):ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPath: String? = null

    private val _playingRecordingId = MutableStateFlow<Long?>(null)
    val playingRecordingId: StateFlow<Long?> = _playingRecordingId.asStateFlow()


    private val _recordings = MutableStateFlow<List<RecordingEntity>>(emptyList())
    val recordings: StateFlow<List<RecordingEntity>> = _recordings.asStateFlow()

    init {
        viewModelScope.launch {
            recordingRepository
                .getAllRecordings()
                .collect { list ->
                    _recordings.value = list
                }
        }
    }

    fun getRecordingById(id: Long): Flow<RecordingEntity?> {
        return recordingRepository.getRecordingById(id)
    }

    fun playRecording(rec: RecordingEntity) {
        val path = rec.filePath

        if (mediaPlayer == null || currentPath != path) {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    // playback finished—reset state
                    _playingRecordingId.value = null
                    release()
                    mediaPlayer = null
                    currentPath = null
                }
            }
            currentPath = path
        } else {
            mediaPlayer?.start()
        }

        _playingRecordingId.value = rec.id
    }

    fun pauseRecording() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        _playingRecordingId.value = null
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }

}
