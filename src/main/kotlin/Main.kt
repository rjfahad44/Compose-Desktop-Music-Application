

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javafx.embed.swing.JFXPanel
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.delay
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.util.concurrent.TimeUnit

@Serializable
data class MusicTrack(
    val title: String,
    val artist: String,
    val image: String?,
    val url: String,
    val isUserAdded: Boolean = false
)

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


val appDataDir = File(System.getProperty("user.home"), "AppData/Roaming/MusicPlayer").also {
    it.mkdirs() // Create the directory if it doesn’t exist
}
// File to store user-added tracks
val storageFile =File(appDataDir, "user_tracks.json")

// Function to save tracks to file
fun saveUserTracks(tracks: List<MusicTrack>) {
    val userTracks = tracks.filter { it.isUserAdded }
    val json = Json { prettyPrint = true }
    storageFile.writeText(json.encodeToString(userTracks))
}

// Function to load tracks from file
fun loadUserTracks(): List<MusicTrack> {
    return if (storageFile.exists()) {
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString(storageFile.readText())
    } else {
        emptyList()
    }
}

// Helper function to load an image from a file path
fun loadImageFromFile(filePath: String): ImageBitmap? {
    return try {
        val file = File(filePath)
        if (file.exists()) {
            val bufferedImage: BufferedImage = ImageIO.read(file)
            bufferedImage.toComposeImageBitmap()
        } else {
            null
        }
    } catch (e: Exception) {
        println("Error loading image: ${e.message}")
        null
    }
}

@Composable
@Preview
fun MusicApp() {
    var currentTrackIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayerState = remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0.0) }
    var duration by remember { mutableStateOf(0.0) }
    var isSeeking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    // Load initial tracks and user-added tracks from storage
    var tracks by remember { mutableStateOf((initialTracks + loadUserTracks()).toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(currentTrackIndex) {
        if (currentTrackIndex >= 0) {
            updateMediaPlayer(currentTrackIndex)
        }
    }

    LaunchedEffect(isPlaying, isSeeking) {
        while (isPlaying && !isSeeking) {
            mediaPlayerState.value?.let { player ->
                currentPosition = player.currentTime.toMillis()
                duration = player.totalDuration.toMillis()
            }
            delay(100)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
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
                        onClick = {
                            currentTrackIndex = tracks.indexOf(track)
                            isPlaying = true
                        },
                        onDelete = if (track.isUserAdded) {
                            {
                                tracks = tracks.toMutableList().apply { remove(track) }
                                saveUserTracks(tracks) // Save updated list after deletion
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
                    onSeek = { percent ->
                        isSeeking = true
                        mediaPlayerState.value?.seek(javafx.util.Duration(duration * percent))
                        currentPosition = duration * percent
                        isSeeking = false
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
                    saveUserTracks(tracks) // Save updated list after addition
                    showAddDialog = false
                }
            )
        }
    }
}


@Composable
fun PlayerControls(
    track: MusicTrack,
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    duration: Double,
    currentPosition: Double
) {
    Card(
        elevation = 8.dp,
        backgroundColor = Color(0xFF1A1A1A),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageBitmap = loadImageFromFile(track.image?:"") ?: painterResource(track.image?:"images/ic_song.webp")
                Image(
                    //painter = painterResource(track.image?:"images/ic_song.webp"),
                    painter = if (imageBitmap is ImageBitmap) BitmapPainter(imageBitmap) else imageBitmap as Painter,
                    modifier = Modifier.size(50.dp, 50.dp).clip(RoundedCornerShape(12.dp)),
                    contentDescription = "Thumb image",
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(track.artist, color = Color.Gray, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.White,
                    fontSize = 12.sp
                )
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFFBB86FC)
                    )
                )
                Text(
                    text = formatTime(duration),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(painterResource("images/ic_previous.xml"), "Previous", tint = Color.White)
                }

                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        IconButton(onClick = onPlayPause) {
                            Icon(
                                painter = painterResource("images/ic_${if (isPlaying) "pause" else "play"}.xml"),
                                "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                                    .background(color = Color(0xFF3D3D3D), shape = CircleShape)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onNext) {
                    Icon(painterResource("images/ic_next.xml"), "Next", tint = Color.White)
                }
            }
        }
    }
}


@Composable
fun TrackItem(track: MusicTrack, isSelected: Boolean, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isSelected) Color(0xFF3D3D3D) else Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageBitmap = loadImageFromFile(track.image?:"") ?: painterResource(track.image?:"images/ic_song.webp")
            Image(
                //painter = painterResource(track.image?:"images/ic_song.webp"),
                painter = if (imageBitmap is ImageBitmap) BitmapPainter(imageBitmap) else imageBitmap as Painter,
                modifier = Modifier.size(80.dp, 100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 0.dp, bottomEnd = 0.dp)),
                contentDescription = "Thumb image",
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)
                Text(track.artist, fontSize = 14.sp, color = Color.Gray)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddSongDialog(onDismiss: () -> Unit, onAddSong: (String, String, String, String) -> Unit) {

    var showErrorDialog by remember { mutableStateOf(false) } // For error alert

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = true,
            usePlatformInsets = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(0.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Song", color = Color.White, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close add dialog", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                var title by remember { mutableStateOf("") }
                var artist by remember { mutableStateOf("") }
                var image by remember { mutableStateOf("") }
                var url by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Image selection with file picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = image,
                        onValueChange = { image = it },
                        label = { Text("Image Path/Url", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                        modifier = Modifier.weight(1f),
                        enabled = false, // Make it read-only
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            disabledBorderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val fileDialog = FileDialog(null as Frame?, "Select Thumbnail", FileDialog.LOAD)
                            fileDialog.isMultipleMode = false
                            fileDialog.file = "*.jpg;*.png" // Filter for image files
                            fileDialog.isVisible = true
                            val selectedFile = fileDialog.files.firstOrNull()
                            if (selectedFile != null) {
                                image = selectedFile.absolutePath // Update with selected file path
                            }
                            if (image.isEmpty()){
                                image = "images/ic_song.webp" // set default image
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00B0FF)),
                        modifier = Modifier.height(60.dp).padding(top = 6.dp)
                    ) {
                        Text("Browse", color = Color.White, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Track URL", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF3B30)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && artist.isNotBlank() && url.isNotBlank()) {
                                onAddSong(title, artist, image, url)
                            } else {
                                showErrorDialog = true // Show error dialog
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3E9F41)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done", color = Color.White, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp))
                    }
                }
            }
        }

        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error", color = Color.White) },
                text = { Text("Please fill all the fields", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBB86FC))
                    ) {
                        Text("OK", color = Color.White)
                    }
                },
                backgroundColor = Color(0xFF1E1E1E),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

fun formatTime(millis: Double): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis.toLong()) % 60
    return String.format("%d:%02d", minutes, seconds)
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