package com.example.audiorecordingapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.audiorecordingapp.MainActivity
import com.example.audiorecordingapp.R
import com.example.audiorecordingapp.data.local.entity.RecordingEntity
import com.example.audiorecordingapp.presentaion.recorder_screen.RecorderVM
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    recorderVM: RecorderVM,
    onBack: () -> Unit,
    onRecordingFinished: () -> Unit
) {
    val ctx = LocalContext.current
    val recordingsRoot = remember {
        File(ctx.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings")
            .apply { if (!exists()) mkdirs() }
    }

    var isRecording by remember { mutableStateOf(false) }
    var currentPath by remember { mutableStateOf("") }
    var recordStartTime by remember { mutableStateOf(0L) }

    // 1) keep track of denials
    var denyCount by rememberSaveable { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 2) launcher for runtime permission
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // reset counter
            denyCount = 0
            // start recording now permission is available
            startRecording(ctx, recordingsRoot) { path, startTime ->
                currentPath = path
                recordStartTime = startTime
                isRecording = true
            }
        } else {
            denyCount++
            if (denyCount >= 3) {
                showSettingsDialog = true
            } else {
                Toast.makeText(ctx, "Audio permission is required to record", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 3) Dialog to send user to Settings after 3 denies
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { /* don't dismiss by tapping outside */ },
            title = { Text("Permission Needed") },
            text = {
                Text("You've denied microphone permission several times. " +
                        "Please enable it in app settings to record audio.")
            },
            confirmButton = {
                TextButton(onClick = {
                    // open app details in Settings
                    ctx.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", ctx.packageName, null)
                        }
                    )
                    showSettingsDialog = false
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recorder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_arrow),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRecording) "Recording…" else "Idle",
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        // if we already have permission, just start;
                        // otherwise fire off the permission flow
                        if (ContextCompat.checkSelfPermission(
                                ctx, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            startRecording(ctx, recordingsRoot) { path, startTime ->
                                currentPath = path
                                recordStartTime = startTime
                                isRecording = true
                            }
                        } else {
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = !isRecording
                ) {
                    Text("Start")
                }

                Button(
                    onClick = {
                        // Stop native recorder
                        val savedPath = MainActivity.nativeStopRecording()
                        val recordEndTime = System.currentTimeMillis()

                        // Build and save your entity
                        val file = File(savedPath)
                        val entity = RecordingEntity(
                            fileName = file.name,
                            filePath = savedPath,
                            createdAt = recordEndTime,
                            duration = recordEndTime - recordStartTime,
                            fileSize = file.length(),
                            format = savedPath.substringAfterLast('.', "wav")
                        )
                        recorderVM.insert(entity)

                        isRecording = false
                        Toast.makeText(ctx, "Saved to:\n$savedPath", Toast.LENGTH_LONG).show()
                        onRecordingFinished()
                    },
                    enabled = isRecording
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

/**
 * Helper function to start recording so we don't repeat the same lines
 * in two separate branches above.
 */
private fun startRecording(
    ctx: Context,
    recordingsRoot: File,
    onStarted: (filePath: String, startTime: Long) -> Unit
) {
    val fileName = "rec_${System.currentTimeMillis()}.wav"
    val path = File(recordingsRoot, fileName).absolutePath

    val res = MainActivity.nativeStartRecording(path)
    if (res == 0) {
        onStarted(path, System.currentTimeMillis())
        Toast.makeText(ctx, "Recording Started", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(ctx, "Start failed: $res", Toast.LENGTH_LONG).show()
    }
}
