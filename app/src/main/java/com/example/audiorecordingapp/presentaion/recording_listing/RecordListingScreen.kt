package com.example.audiorecordingapp.presentaion.recording_listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.audiorecordingapp.R
import com.example.audiorecordingapp.data.local.entity.RecordingEntity
import com.example.audiorecordingapp.ui.theme.AudioRecordingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListingScreen(
    recordings: List<RecordingEntity>,
    playingId: Long?,
    onStartRecording: () -> Unit,
    onPlay: (RecordingEntity) -> Unit,
    onPause: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recordings") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartRecording) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add recording"
                )
            }
        },
        content = { padding ->
            if (recordings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recordings yet")
                }
            } else {
                LazyColumn(
                    contentPadding = padding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(recordings) { recording ->
                        RecordingItem(
                            recording = recording,
                            isPlaying = (recording.id == playingId),
                            onPlay = { onPlay(recording) },
                            onPause = onPause
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun RecordingItem(
    recording: RecordingEntity,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (isPlaying) onPause() else onPlay() }) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause else R.drawable.play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recording.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recording.createdAt.toFormattedDate(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = recording.duration.toMinSeconds(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = recording.fileSize.toReadableSize(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = recording.format.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun Long.toMinSeconds(): String {
    val totalSec = this / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

private fun Long.toReadableSize(): String {
    val kb = this / 1024
    return if (kb < 1024) "${kb}KB" else "${kb / 1024}MB"
}

private fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewRecordings = listOf(
    RecordingEntity(
        id = 1L,
        fileName = "rec_1715000000000.wav",
        filePath = "/storage/music/rec_1715000000000.wav",
        createdAt = 1715000000000L,
        duration = 65_000L,
        fileSize = 1_048_576L,
        format = "wav"
    ),
    RecordingEntity(
        id = 2L,
        fileName = "rec_1714913600000.wav",
        filePath = "/storage/music/rec_1714913600000.wav",
        createdAt = 1714913600000L,
        duration = 125_000L,
        fileSize = 2_097_152L,
        format = "wav"
    )
)

@Preview(showBackground = true, name = "List – empty")
@Composable
private fun RecordListingScreenEmptyPreview() {
    AudioRecordingAppTheme {
        RecordListingScreen(
            recordings = emptyList(),
            playingId = null,
            onStartRecording = {},
            onPlay = {},
            onPause = {}
        )
    }
}

@Preview(showBackground = true, name = "List – with recordings")
@Composable
private fun RecordListingScreenPopulatedPreview() {
    AudioRecordingAppTheme {
        RecordListingScreen(
            recordings = previewRecordings,
            playingId = 1L,
            onStartRecording = {},
            onPlay = {},
            onPause = {}
        )
    }
}

@Preview(showBackground = true, name = "RecordingItem – idle")
@Composable
private fun RecordingItemIdlePreview() {
    AudioRecordingAppTheme {
        RecordingItem(
            recording = previewRecordings.first(),
            isPlaying = false,
            onPlay = {},
            onPause = {}
        )
    }
}

@Preview(showBackground = true, name = "RecordingItem – playing")
@Composable
private fun RecordingItemPlayingPreview() {
    AudioRecordingAppTheme {
        RecordingItem(
            recording = previewRecordings.first(),
            isPlaying = true,
            onPlay = {},
            onPause = {}
        )
    }
}
