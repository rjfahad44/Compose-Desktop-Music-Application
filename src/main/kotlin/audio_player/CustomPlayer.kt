package audio_player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    var currentTrackIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0.0) }
    var duration by remember { mutableStateOf(0.0) }
    var isSeeking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Notify UI of state changes
    LaunchedEffect(
        currentTrackIndex,
        isPlaying,
        isLoading,
        currentPosition,
        duration
    ) {
        onPlayerStateChanged(
            PlayerState(
                currentTrack = tracks.getOrNull(currentTrackIndex),
                isPlaying = isPlaying,
                isLoading = isLoading,
                currentPosition = currentPosition,
                duration = duration,
                currentTrackIndex = currentTrackIndex
            )
        )
    }

    // Progress update loop
    DisposableEffect(mediaPlayerState) {
        val progressJob = scope.launch {
            while (isActive) {
                mediaPlayerState.value?.let { player ->
                    if (!isSeeking) {
                        currentPosition = player.currentTime.toMillis()
                    }
                    duration = player.totalDuration.toMillis()
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
            isLoading = false
            isPlaying = false
            currentTrackIndex = -1
            onNextTrack()
            return
        }

        cleanupMediaPlayer(mediaPlayerState)

        isLoading = true
        val mediaUrl = url.toNormalizeMediaUrl()
        val player = createMediaPlayer(
            mediaUrl = mediaUrl,
            onReady = {
                duration = it.totalDuration.toMillis()
                isLoading = false
                it.play()
            },
            onEndOfMedia = { onNextTrack() },
            onPlay = {
                isPlaying = true
            },
            onPause = {
                isPlaying = false
            },
            onError = {
                println("Error for ${track.title}: ${it.message}")
                isLoading = false
                isPlaying = false
                onNextTrack()
            }
        )

        mediaPlayerState.value = player
        currentTrackIndex = index
    }

    // Function to try the next track
    fun tryNextTrack() {
        if (currentTrackIndex < tracks.size - 1) {
            currentTrackIndex++
            playTrack(currentTrackIndex, onNextTrack = { tryNextTrack() })
        } else {
            isPlaying = false
            currentTrackIndex = -1
        }
    }

    // External controls
    fun playPause() {
        mediaPlayerState.value?.let { player ->
            if (isPlaying) player.pause() else player.play()
            isPlaying = !isPlaying
        }
    }

    fun seekTo(percent: Float) {
        val newPosition = duration * percent
        currentPosition = newPosition
        mediaPlayerState.value?.seek(Duration(newPosition))
    }

    fun previousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--
            playTrack(currentTrackIndex, onNextTrack = { tryNextTrack() })
        }
    }

    fun nextTrack() {
        tryNextTrack()
    }

    fun playTrackAtIndex(index: Int) {
        if (index in tracks.indices) {
            currentTrackIndex = index
            playTrack(index, onNextTrack = { tryNextTrack() })
        }
    }

    // Expose controller to UI
    LaunchedEffect(Unit) {
        onControllerChanged(
            PlayerController(
                playPause = { playPause() },
                seekTo = { newPosition ->
                    isSeeking = true
                    currentPosition = (duration * newPosition)
                },
                onSeekFinished = { percent ->
                    isSeeking = false
                    seekTo(percent)
                },
                previousTrack = { previousTrack() },
                nextTrack = { nextTrack() },
                playTrackAtIndex = { index -> playTrackAtIndex(index) }
            )
        )
    }

    // Start playback when track index changes
    LaunchedEffect(currentTrackIndex) {
        if (currentTrackIndex in tracks.indices) {
            playTrack(currentTrackIndex, onNextTrack = { tryNextTrack() })
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
        val player = MediaPlayer(Media(mediaUrl))

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