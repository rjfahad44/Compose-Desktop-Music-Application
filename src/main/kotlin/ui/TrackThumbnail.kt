package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import models.MusicTrack
import util.isImageUrl
import util.loadImageBitmapFromFile
import util.loadImageBitmapFromUrl
import java.io.File

@Composable
fun TrackThumbnail(
    track: MusicTrack,
    modifier: Modifier = Modifier
) {
    val imageModifier = modifier
        .size(60.dp, 70.dp)
        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))

    var imageBitmap by remember(track.image) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(track.image) { mutableStateOf(false) }

    LaunchedEffect(track.image) {
        if (track.image.isNullOrEmpty() || track.image == "images/ic_song.webp") {
            imageBitmap = null
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        imageBitmap = when {
            File(track.image).exists() -> {
                loadImageBitmapFromFile(track.image)
            }
            track.image.isImageUrl() -> {
                loadImageBitmapFromUrl(track.image)
            }
            else -> {
                println("Invalid image path: ${track.image}")
                null
            }
        }
        isLoading = false
    }

    Box(
        modifier = imageModifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = imageBitmap?.let { BitmapPainter(it) } ?: painterResource(if (track.image?.startsWith("images", ignoreCase = true) == true) track.image else "images/ic_song.webp"),
            contentDescription = "Track thumbnail",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF00B0FF),
                strokeWidth = 2.dp
            )
        }
    }
}
