import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import models.MusicTrack
import models.PlayerController
import models.PlayerState
import ui.AddSongDialog
import player.CustomPlayer
import ui.PlayerControls
import ui.TrackItem
import util.*
import java.awt.Dimension


@Composable
@Preview
fun MusicApp() {

    val tracks = remember { mutableStateOf((initialTracks + loadUserTracks()).toMutableList()) }
    val currentTrack = remember { mutableStateOf<MusicTrack?>(null) }
    val showAddDialog = remember { mutableStateOf(false) }
    val playerState = remember { mutableStateOf<PlayerState?>(null) }
    val playerController = remember { mutableStateOf<PlayerController?>(null) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            TopAppBar(
                title = { Text("Music Player", color = Color.White) },
                backgroundColor = Color.Black,
                actions = {
//                    IconButton(onClick = {
//
//                    }) {
//                        Icon(Icons.Default.ThumbUp, contentDescription = "Open Shorts", tint = Color.White)
//                    }
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
                        isSelected = tracks.value.indexOf(track) == playerState.value?.currentTrackIndex,
                        isPlaying = playerState.value?.isPlaying ?: false,
                        onClick = {
                            playerController.value?.playTrackAtIndex(tracks.value.indexOf(track))
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

            playerState.value?.let { state ->
                AnimatedVisibility(visible = state.currentTrackIndex in tracks.value.indices) {
                    PlayerControls(
                        track = state.currentTrack,
                        isPlaying = state.isPlaying,
                        isLoading = state.isLoading,
                        progress = if (state.duration > 0) (state.currentPosition / state.duration).toFloat() else 0f,
                        controller = playerController.value ?: return@AnimatedVisibility,
                        duration = state.duration,
                        currentPosition = state.currentPosition
                    )
                }
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

        CustomPlayer(
            tracks = tracks.value,
            onPlayerStateChanged = { state ->
                playerState.value = state
            },
            onControllerChanged = { controller ->
                playerController.value = controller
            }
        )
    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("images/icon.ico"),
        title = "Music Player",
        state = rememberWindowState(
            size = DpSize(500.dp, 500.dp),
            position = WindowPosition.Aligned(Alignment.Center) // Center on screen
        ),
    ) {
        window.minimumSize = Dimension(500, 500)
        MusicApp()
    }
}