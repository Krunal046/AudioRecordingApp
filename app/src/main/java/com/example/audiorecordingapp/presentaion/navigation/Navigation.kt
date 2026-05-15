package com.example.audiorecordingapp.presentaion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.example.audiorecordingapp.presentaion.recorder_screen.RecorderVM
import com.example.audiorecordingapp.presentaion.recording_listing.RecordListingScreen
import com.example.audiorecordingapp.presentaion.recording_listing.RecordingListingVM
import com.example.audiorecordingapp.ui.RecordingScreen

sealed class Screen(val route: String) {
    object Listing : Screen("recordings")
    object Recorder : Screen("recorder")
}

@Composable
fun AudioNavGraph(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Listing.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Listing.route) {
            val vm: RecordingListingVM = hiltViewModel()
            val recordings by vm.recordings.collectAsState()
            val playingId by vm.playingRecordingId.collectAsState()
            RecordListingScreen(
                recordings = recordings,
                playingId = playingId,
                onStartRecording = { navController.navigate(Screen.Recorder.route) },
                onPlay = { vm.playRecording(it) },
                onPause = { vm.pauseRecording() }
            )
        }
        composable(Screen.Recorder.route) {
            val recorderVM: RecorderVM = hiltViewModel()
            RecordingScreen(
                recorderVM = recorderVM,
                onBack = { navController.popBackStack() },
                onRecordingFinished = { navController.popBackStack() }
            )
        }
    }
}
