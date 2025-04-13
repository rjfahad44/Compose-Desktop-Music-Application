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
import util.isValidMediaUrl
import util.loadUserTracks
import util.saveUserTracks
import java.io.File

val initialTracks = listOf(
    MusicTrack(
        "Calm Nights",
        "Lofi Chill",
        "images/1.jpg",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    ),
    MusicTrack(
        "Sky Dreams",
        "DJ Relax",
        "images/2.jpg",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
    ),
    MusicTrack(
        "Focus Vibes",
        "Ambient Sound",
        "images/3.jpg",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
    ),
    MusicTrack(
        "Deep Flow",
        "BeatMaster",
        "images/4.jpg",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
    ),
    MusicTrack(
        "Sky Dreams",
        "DJ Relax",
        "images/5.jpg",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song Maltese"
    ),
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
    var currentTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Coroutine scope for managing seek updates
    val scope = rememberCoroutineScope()

    fun updateMediaPlayer(trackIndex: Int) {
        // Stop and dispose of existing player
        mediaPlayerState.value?.stop()
        mediaPlayerState.value?.dispose()
        mediaPlayerState.value = null

        if (trackIndex !in tracks.indices) {
            isLoading = false
            isPlaying = false
            currentTrackIndex = -1
            println("Invalid track index: $trackIndex")
            return
        }

        val track = tracks[trackIndex]
        val url = track.url

        // Validate URL
        if (!url.isValidMediaUrl()) {
            println("Invalid media URL for track ${track.title}: $url")
            isLoading = false
            isPlaying = false
            currentTrackIndex = -1
            // Optionally skip to next track
            if (trackIndex < tracks.size - 1) {
                currentTrackIndex = trackIndex + 1
                updateMediaPlayer(currentTrackIndex)
            }
            return
        }

        isLoading = true
        try {
            // Normalize URL for local files
            val mediaUrl = if (File(url).exists()) {
                File(url).toURI().toString() // Convert to file:// URI
            } else {
                url // Assume network URL
            }

            // Initialize JavaFX (if not already done)
            JFXPanel() // Consider moving to app initialization
            val media = Media(mediaUrl)
            val player = MediaPlayer(media)

            player.setOnReady {
                duration = player.totalDuration.toMillis()
                isLoading = false
                if (isPlaying) player.play()
            }

            player.setOnEndOfMedia {
                if (trackIndex < tracks.size - 1) {
                    currentTrackIndex = trackIndex + 1
                    updateMediaPlayer(currentTrackIndex)
                } else {
                    isPlaying = false
                    currentTrackIndex = -1
                }
            }

            player.setOnError {
                println("Media player error for ${track.title}: ${player.error}")
                isLoading = false
                isPlaying = false
                // Optionally skip to next track
                if (trackIndex < tracks.size - 1) {
                    currentTrackIndex = trackIndex + 1
                    updateMediaPlayer(currentTrackIndex)
                }
            }

            mediaPlayerState.value = player
        } catch (e: Exception) {
            println("Failed to initialize media player for ${track.title}: ${e.message}")
            isLoading = false
            isPlaying = false
            currentTrackIndex = -1
            // Optionally skip to next track
            if (trackIndex < tracks.size - 1) {
                currentTrackIndex = trackIndex + 1
                updateMediaPlayer(currentTrackIndex)
            }
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
                    IconButton(onClick = {
                        currentTrack = null
                        showAddDialog = true
                    }) {
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
                        onEdit = if (track.isUserAdded) {
                            {
                                currentTrack = track
                                showAddDialog = true
                            }
                        } else null,
                        onDelete = if (track.isUserAdded) {
                            {
                                currentTrack = null
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
                track = currentTrack,
                onAddSong = { title, artist, image, url ->
                    tracks = tracks.toMutableList().apply {
                        remove(currentTrack)
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