package com.example.audiorecordingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.audiorecordingapp.presentaion.navigation.AudioNavGraph
import com.example.audiorecordingapp.ui.theme.AudioRecordingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        init { System.loadLibrary("native-recorder") }
        @JvmStatic external fun nativeStartRecording(outputPath: String): Int
        @JvmStatic external fun nativeStopRecording(): String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioRecordingAppTheme {
                AudioNavGraph()
            }
        }
    }
}


