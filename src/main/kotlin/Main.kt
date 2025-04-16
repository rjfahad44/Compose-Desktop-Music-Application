import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import models.MusicTrack
import util.*


// Function to create and configure a MediaPlayer
fun createMediaPlayer(
    mediaUrl: String,
    onReady: (MediaPlayer) -> Unit,
    onEndOfMedia: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onError: (Exception) -> Unit
): MediaPlayer? {
    return try {
        JFXPanel() // Initialize JavaFX (consider moving to app startup)
        val media = Media(mediaUrl)
        val player = MediaPlayer(media)

        player.setOnReady { onReady(player) }
        player.setOnEndOfMedia { onEndOfMedia() }
        player.setOnError { onError(Exception(player.error.toString())) }

        // Listen to status changes
        player.statusProperty().addListener { _, _, newStatus ->
            when (newStatus) {
                MediaPlayer.Status.PLAYING -> onPlay()
                MediaPlayer.Status.PAUSED -> onPause()
                else -> Unit
            }
        }

        player
    } catch (e: Exception) {
        println("Failed to create media player: ${e.message}")
        null
    }
}

// Function to handle track playback logic
fun playTrack(
    trackIndex: Int,
    tracks: List<MusicTrack>,
    mediaPlayerState: MutableState<MediaPlayer?>,
    isPlaying: MutableState<Boolean>,
    isLoading: MutableState<Boolean>,
    currentTrackIndex: MutableState<Int>,
    duration: MutableState<Double>,
    onNextTrack: () -> Unit
) {
    val track = tracks.getOrNull(trackIndex) ?: return
    val url = track.url
    if (!url.isValidMediaUrl()) {
        println("Invalid media URL for track ${track.title}: $url")
        isLoading.value = false
        isPlaying.value = false
        currentTrackIndex.value = -1
        onNextTrack()
        return
    }

    cleanupMediaPlayer(mediaPlayerState)

    isLoading.value = true
    val mediaUrl = url.toNormalizeMediaUrl()
    val player = createMediaPlayer(
        mediaUrl = mediaUrl,
        onReady = {
            duration.value = it.totalDuration.toMillis()
            isLoading.value = false
            it.play()
        },
        onEndOfMedia = onNextTrack,
        onPlay = {
            isPlaying.value = true
        },
        onPause = {
            isPlaying.value = false
        },
        onError = {
            println("Error for ${track.title}: ${it.message}")
            isLoading.value = false
            isPlaying.value = false
            onNextTrack()
        }
    )

    mediaPlayerState.value = player
}


// Function to clean up the existing media player
fun cleanupMediaPlayer(mediaPlayerState: MutableState<MediaPlayer?>) {
    mediaPlayerState.value?.stop()
    mediaPlayerState.value?.dispose()
    mediaPlayerState.value = null
}


@Composable
@Preview
fun MusicApp() {
    val currentTrackIndex = remember { mutableStateOf(-1) }
    val isPlaying = remember { mutableStateOf(false) }
    val isSeeking = remember { mutableStateOf(false) }
    val mediaPlayerState = remember { mutableStateOf<MediaPlayer?>(null) }
    val currentPosition = remember { mutableStateOf(0.0) }
    val duration = remember { mutableStateOf(0.0) }
    val isLoading = remember { mutableStateOf(false) }
    val tracks = remember { mutableStateOf((initialTracks + loadUserTracks()).toMutableList()) }
    val currentTrack = remember { mutableStateOf<MusicTrack?>(null) }
    val showAddDialog = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun tryNextTrack() {
        if (currentTrackIndex.value < tracks.value.size - 1) {
            currentTrackIndex.value++
            playTrack(
                trackIndex = currentTrackIndex.value,
                tracks = tracks.value,
                mediaPlayerState = mediaPlayerState,
                isPlaying = isPlaying,
                isLoading = isLoading,
                currentTrackIndex = currentTrackIndex,
                duration = duration,
                onNextTrack = { tryNextTrack() }
            )
        } else {
            isPlaying.value = false
            currentTrackIndex.value = -1
        }
    }

    LaunchedEffect(currentTrackIndex.value) {
        if (currentTrackIndex.value in tracks.value.indices) {
            playTrack(
                trackIndex = currentTrackIndex.value,
                tracks = tracks.value,
                mediaPlayerState = mediaPlayerState,
                isPlaying = isPlaying,
                isLoading = isLoading,
                currentTrackIndex = currentTrackIndex,
                duration = duration,
                onNextTrack = { tryNextTrack() }
            )
        }
    }

    DisposableEffect(mediaPlayerState) {
        val progressJob = scope.launch {
            while (isActive) {
                mediaPlayerState.value?.let { player ->
                    if (!isSeeking.value) {
                        currentPosition.value = player.currentTime.toMillis()
                    }
                    duration.value = player.totalDuration.toMillis()
                }
                delay(250L)
            }
        }

        onDispose {
            progressJob.cancel()
            cleanupMediaPlayer(mediaPlayerState)
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            TopAppBar(
                title = { Text("Music Player", color = Color.White) },
                backgroundColor = Color.Black,
                actions = {
                    IconButton(onClick = {
                        currentTrack.value = null
                        showAddDialog.value = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Song", tint = Color.White)
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tracks.value) { track ->
                    TrackItem(
                        track = track,
                        isSelected = tracks.value.indexOf(track) == currentTrackIndex.value,
                        isPlaying = isPlaying.value,
                        onClick = {
                            currentTrackIndex.value = tracks.value.indexOf(track)
                        },
                        onEdit = if (track.isUserAdded) {
                            {
                                currentTrack.value = track
                                showAddDialog.value = true
                            }
                        } else null,
                        onDelete = if (track.isUserAdded) {
                            {
                                currentTrack.value = null
                                tracks.value = tracks.value.toMutableList().apply { remove(track) }
                                saveUserTracks(tracks.value)
                            }
                        } else null
                    )
                }
            }

            AnimatedVisibility(visible = currentTrackIndex.value in tracks.value.indices) {
                PlayerControls(
                    track = tracks.value[currentTrackIndex.value],
                    isPlaying = isPlaying.value,
                    isLoading = isLoading.value,
                    progress = if (duration.value > 0) (currentPosition.value / duration.value).toFloat() else 0f,
                    onPlayPause = {
                        mediaPlayerState.value?.let { player ->
                            if (isPlaying.value) player.pause() else player.play()
                            isPlaying.value = !isPlaying.value
                        }
                    },
                    onSeek = { newPosition ->
                        isSeeking.value = true
                        currentPosition.value = (duration.value * newPosition)
                    },
                    onSeekFinished = { percent ->
                        val newPosition = duration.value * percent
                        currentPosition.value = newPosition
                        mediaPlayerState.value?.seek(Duration(newPosition))
                        isSeeking.value = false
                    },
                    onPrevious = {
                        if (currentTrackIndex.value > 0) {
                            currentTrackIndex.value--
                            isPlaying.value = true
                        }
                    },
                    onNext = {
                        tryNextTrack()
                    },
                    duration = duration.value,
                    currentPosition = currentPosition.value
                )
            }
        }

        if (showAddDialog.value) {
            AddSongDialog(
                onDismiss = { showAddDialog.value = false },
                track = currentTrack.value,
                onAddSong = { title, artist, image, url ->
                    tracks.value = tracks.value.toMutableList().apply {
                        remove(currentTrack.value)
                        add(MusicTrack(title, artist, image, url, isUserAdded = true))
                    }
                    saveUserTracks(tracks.value)
                    showAddDialog.value = false
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