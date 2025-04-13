

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javafx.embed.swing.JFXPanel
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.MusicTrack
import util.loadUserTracks
import util.saveUserTracks

val initialTracks = listOf(
    MusicTrack("Calm Nights", "Lofi Chill", "images/1.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
    MusicTrack("Sky Dreams", "DJ Relax", "images/2.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
    MusicTrack("Focus Vibes", "Ambient Sound", "images/3.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
    MusicTrack("Deep Flow", "BeatMaster", "images/4.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"),
    MusicTrack("Calm Nights", "Lofi Chill", "images/5.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
    MusicTrack("Sky Dreams", "DJ Relax", "images/6.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song Maltese"),
    MusicTrack("Focus Vibes", "Ambient Sound", "images/3.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
    MusicTrack("Deep Flow", "BeatMaster", "images/4.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3")
)

@Composable
@Preview
fun MusicApp() {
    var currentTrackIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    val mediaPlayerState = remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0.0) }
    var duration by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var tracks by remember { mutableStateOf((initialTracks + loadUserTracks()).toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Coroutine scope for managing seek updates
    val scope = rememberCoroutineScope()

    fun updateMediaPlayer(trackIndex: Int) {
        mediaPlayerState.value?.stop()
        mediaPlayerState.value?.dispose()

        if (trackIndex in tracks.indices) {
            isLoading = true
            JFXPanel()
            val media = Media(tracks[trackIndex].url)
            val player = MediaPlayer(media)

            player.setOnReady {
                duration = player.totalDuration.toMillis()
                isLoading = false
                if (isPlaying) player.play()
            }

            player.setOnEndOfMedia {
                if (trackIndex < tracks.size - 1) {
                    currentTrackIndex = trackIndex + 1
                } else {
                    isPlaying = false
                    currentTrackIndex = -1
                }
            }

            player.setOnError {
                println("Media player error: ${player.error}")
                isLoading = false
            }

            mediaPlayerState.value = player
        }
    }

    // Update player when track changes
    LaunchedEffect(currentTrackIndex) {
        if (currentTrackIndex >= 0) {
            updateMediaPlayer(currentTrackIndex)
        }
    }

    // Cleanup media player on dispose
    DisposableEffect(mediaPlayerState) {
        val progressJob = scope.launch {
            while (true) {
                mediaPlayerState.value?.let { player ->
                    if (!isSeeking) currentPosition = player.currentTime.toMillis()
                    duration = player.totalDuration.toMillis()
                }
                delay(100L)
            }
        }

        onDispose {
            progressJob.cancel()
            mediaPlayerState.value?.stop()
            mediaPlayerState.value?.dispose()
            mediaPlayerState.value = null
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            TopAppBar(
                title = { Text("Music Player", color = Color.White) },
                backgroundColor = Color.Black,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Song", tint = Color.White)
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tracks) { track ->
                    TrackItem(
                        track = track,
                        isSelected = tracks.indexOf(track) == currentTrackIndex,
                        isPlaying = isPlaying,
                        onClick = {
                            currentTrackIndex = tracks.indexOf(track)
                            isPlaying = true
                        },
                        onDelete = if (track.isUserAdded) {
                            {
                                tracks = tracks.toMutableList().apply { remove(track) }
                                saveUserTracks(tracks)
                            }
                        } else null
                    )
                }
            }

            if (currentTrackIndex in tracks.indices) {
                PlayerControls(
                    track = tracks[currentTrackIndex],
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    progress = if (duration > 0) (currentPosition / duration).toFloat() else 0f,
                    onPlayPause = {
                        mediaPlayerState.value?.let { player ->
                            if (isPlaying) player.pause() else player.play()
                            isPlaying = !isPlaying
                        }
                    },
                    onSeek = { newPosition ->
                        isSeeking = true
                        currentPosition = (duration * newPosition)
                    },
                    onSeekFinished = remember(mediaPlayerState) {
                        { percent ->
                            val newPosition = duration * percent
                            currentPosition = newPosition
                            mediaPlayerState.value?.seek(Duration(newPosition))
                            isSeeking = false
                        }
                    },
                    onPrevious = {
                        if (currentTrackIndex > 0) {
                            currentTrackIndex--
                            isPlaying = true
                        }
                    },
                    onNext = {
                        if (currentTrackIndex < tracks.size - 1) {
                            currentTrackIndex++
                            isPlaying = true
                        }
                    },
                    duration = duration,
                    currentPosition = currentPosition
                )
            }
        }

        if (showAddDialog) {
            AddSongDialog(
                onDismiss = { showAddDialog = false },
                onAddSong = { title, artist, image, url ->
                    tracks = tracks.toMutableList().apply {
                        add(MusicTrack(title, artist, image, url, isUserAdded = true))
                    }
                    saveUserTracks(tracks)
                    showAddDialog = false
                }
            )
        }
    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("images/icon.ico"),
        title = "Music Player"
    ) {
        MusicApp()
    }
}