package com.example.audiorecordingapp.presentaion.recorder_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiorecordingapp.data.local.entity.RecordingEntity
import com.example.audiorecordingapp.data.local.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecorderVM @Inject constructor(
    private val recordingRepository: RecordingRepository

):ViewModel(){

    fun insert(recording: RecordingEntity) {
        viewModelScope.launch {
            recordingRepository.insertRecording(recording)
        }
    }

}
