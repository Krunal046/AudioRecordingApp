package com.example.audiorecordingapp.presentaion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

@Composable
fun AudioPermissionHandler(
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    // how many times the user has denied
    var denialCount by rememberSaveable { mutableStateOf(0) }
    // whether to show our rationale dialog
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onGranted()
        } else {
            denialCount++
            if (denialCount >= 3) {
                // third denial → send them to Settings
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    ).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
            } else {
                // show rationale so they can try again
                showRationale = true
            }
        }
    }

    // Kick off the permission check/request on first composition
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // already have it
                onGranted()
            }
            // if they've denied once but not permanently, show rationale
            shouldShowRequestPermissionRationale(
                context as Activity, Manifest.permission.RECORD_AUDIO
            ) -> {
                showRationale = true
            }
            else -> {
                // first-time request
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Our custom rationale dialog
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { /* prevent dismiss */ },
            title = { Text("Microphone Permission Needed") },
            text = { Text("We need access to your microphone to record audio. Please allow it.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    // user chose not to try again now
                }) {
                    Text("Deny")
                }
            }
        )
    }
}
