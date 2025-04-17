package player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import javafx.embed.swing.JFXPanel
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import models.MusicTrack
import models.PlayerController
import models.PlayerState
import util.isValidMediaUrl
import util.toNormalizeMediaUrl

@Composable
fun CustomPlayer(
    tracks: List<MusicTrack>,
    onPlayerStateChanged: (PlayerState) -> Unit,
    onControllerChanged: (PlayerController) -> Unit,
) {
    val mediaPlayerState = remember { mutableStateOf<MediaPlayer?>(null) }
    val currentTrackIndex = remember { mutableStateOf(-1) }
    val isPlaying = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val currentPosition = remember { mutableStateOf(0.0) }
    val duration = remember { mutableStateOf(0.0) }
    val isSeeking = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Notify UI of state changes
    LaunchedEffect(
        currentTrackIndex.value,
        isPlaying.value,
        isLoading.value,
        currentPosition.value,
        duration.value
    ) {
        onPlayerStateChanged(
            PlayerState(
                currentTrack = tracks.getOrNull(currentTrackIndex.value),
                isPlaying = isPlaying.value,
                isLoading = isLoading.value,
                currentPosition = currentPosition.value,
                duration = duration.value,
                currentTrackIndex = currentTrackIndex.value
            )
        )
    }

    // Progress update loop
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

    // Function to play a track
    fun playTrack(index: Int, onNextTrack: () -> Unit) {
        val track = tracks.getOrNull(index) ?: return
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
            onEndOfMedia = { onNextTrack() },
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
        currentTrackIndex.value = index
    }

    // Function to try the next track
    fun tryNextTrack() {
        if (currentTrackIndex.value < tracks.size - 1) {
            currentTrackIndex.value++
            playTrack(currentTrackIndex.value, onNextTrack = { tryNextTrack() })
        } else {
            isPlaying.value = false
            currentTrackIndex.value = -1
        }
    }

    // External controls
    fun playPause() {
        mediaPlayerState.value?.let { player ->
            if (isPlaying.value) player.pause() else player.play()
            isPlaying.value = !isPlaying.value
        }
    }

    fun seekTo(percent: Float) {
        val newPosition = duration.value * percent
        currentPosition.value = newPosition
        mediaPlayerState.value?.seek(Duration(newPosition))
    }

    fun previousTrack() {
        if (currentTrackIndex.value > 0) {
            currentTrackIndex.value--
            playTrack(currentTrackIndex.value, onNextTrack = { tryNextTrack() })
        }
    }

    fun nextTrack() {
        tryNextTrack()
    }

    fun playTrackAtIndex(index: Int) {
        if (index in tracks.indices) {
            currentTrackIndex.value = index
            playTrack(index, onNextTrack = { tryNextTrack() })
        }
    }

    // Expose controller to UI
    LaunchedEffect(Unit) {
        onControllerChanged(
            PlayerController(
                playPause = { playPause() },
                seekTo = { newPosition ->
                    isSeeking.value = true
                    currentPosition.value = (duration.value * newPosition)
                },
                onSeekFinished = { percent -> seekTo(percent) },
                previousTrack = { previousTrack() },
                nextTrack = { nextTrack() },
                playTrackAtIndex = { index -> playTrackAtIndex(index) }
            )
        )
    }

    // Start playback when track index changes
    LaunchedEffect(currentTrackIndex.value) {
        if (currentTrackIndex.value in tracks.indices) {
            playTrack(currentTrackIndex.value, onNextTrack = { tryNextTrack() })
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            cleanupMediaPlayer(mediaPlayerState)
        }
    }
}

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

// Function to clean up the existing media player
fun cleanupMediaPlayer(mediaPlayerState: MutableState<MediaPlayer?>) {
    mediaPlayerState.value?.stop()
    mediaPlayerState.value?.dispose()
    mediaPlayerState.value = null
}