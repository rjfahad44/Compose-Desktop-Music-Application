
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import audio_player.CustomPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.MusicTrack
import models.PlayerController
import models.PlayerState
import ui.AddSongDialog
import ui.PlayerControls
import ui.TrackItem
import util.initialTracks
import util.loadUserTracks
import util.saveUserTracks
import java.awt.Dimension
import kotlin.random.Random


@Composable
@Preview
fun MusicApp(windowState: WindowState) {

    var tracks by remember { mutableStateOf((initialTracks + loadUserTracks()).toMutableList()) }
    var currentTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var playerState by remember { mutableStateOf<PlayerState?>(null) }
    var playerController by remember { mutableStateOf<PlayerController?>(null) }
    val scope = rememberCoroutineScope()

    // Target size to animate to
    var targetWidth by remember { mutableStateOf(480.dp) }
    val animatedWidth = remember { Animatable(targetWidth, Dp.VectorConverter) }
    // Animate width
    LaunchedEffect(targetWidth) {
        animatedWidth.animateTo(
            targetWidth,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
        )
    }
    windowState.size = DpSize(animatedWidth.value, 700.dp)

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            var rotation by remember { mutableStateOf(0f) }
            val animatedRotation by animateFloatAsState(
                targetValue = rotation,
                animationSpec = tween(
                    durationMillis = 300, // smoother duration
                    easing = LinearEasing // constant speed
                ),
                label = "iconRotation"
            )
            TopAppBar(
                title = { Text("Music Player", color = Color.White) },
                backgroundColor = Color.Black,
                actions = {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            App.appModule.redditDataRepository.fetchData().collect {
                                println("RedditData: $it")
                            }
                        }
                        rotation += 360f // Rotate once every click
                        targetWidth = Random.nextInt(480, 800).dp
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Open Shorts",
                            tint = Color.White,
                            modifier = Modifier.rotate(animatedRotation)
                        )
                    }
                    IconButton(onClick = {
                        currentTrack = null
                        showAddDialog = true
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
                items(tracks) { track ->
                    TrackItem(
                        track = track,
                        isSelected = tracks.indexOf(track) == playerState?.currentTrackIndex,
                        isPlaying = playerState?.isPlaying == true,
                        onClick = {
                            playerController?.playTrackAtIndex(tracks.indexOf(track))
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

            playerState?.let { state ->
                AnimatedVisibility(visible = state.currentTrackIndex in tracks.indices) {
                    PlayerControls(
                        track = state.currentTrack,
                        isPlaying = state.isPlaying,
                        isLoading = state.isLoading,
                        progress = if (state.duration > 0) (state.currentPosition / state.duration).toFloat() else 0f,
                        controller = playerController ?: return@AnimatedVisibility,
                        duration = state.duration,
                        currentPosition = state.currentPosition
                    )
                }
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

        CustomPlayer(
            tracks = tracks,
            onPlayerStateChanged = { state ->
                playerState = state
            },
            onControllerChanged = { controller ->
                playerController = controller
            }
        )
    }
}

fun main() = application {
    App.initializeModule()
    val windowState = rememberWindowState(
        size = DpSize(480.dp, 700.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("images/icon.ico"),
        title = "Music Player",
        state = windowState,
    ) {
        window.minimumSize = Dimension(480, 700)
        MusicApp(windowState)
        val url = "https://v.redd.it/g7694jen9sve1/HLSPlaylist.m3u8?a=1747716365%2CNDY5ZjZiOGIyMTQzYzhjMGI0ZTAwY2Q4MTEwZWJiMTEzYWRmYmQ4YzFiMjNjNmEyNWI4MWJlMGMzNGJiNDQ5Mw%3D%3D&v=1&f=sd"
        //val videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        //CustomVideoPlayer(videoUrl.toNormalizeMediaUrl())
    }
}
