package util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import data.models.RedditResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.MusicTrack
import models.VideoData
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.div
import kotlin.text.orEmpty


val appDataDir = File(System.getProperty("user.home"), "AppData/Roaming/MusicPlayer").also {
    it.mkdirs() // Create the directory if it doesn’t exist
}
// File to store user-added tracks
val storageFile = File(appDataDir, "user_tracks.json")

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

// Load image from local file path
fun loadImageBitmapFromFile(filePath: String): ImageBitmap? {
    return try {
        val file = File(filePath)
        if (file.exists()) {
            val bufferedImage: BufferedImage = ImageIO.read(file)
            bufferedImage.toComposeImageBitmap()
        } else {
            null
        }
    } catch (e: Exception) {
        println("Error loading image from file: $filePath, ${e.message}")
        null
    }
}

// Load image from URL with basic caching
suspend fun loadImageBitmapFromUrl(url: String): ImageBitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (url.isImageUrl()) {
                val cacheFile = File(System.getProperty("java.io.tmpdir"), url.hashCode().toString())
                if (cacheFile.exists()) {
                    val bufferedImage = ImageIO.read(cacheFile)
                    return@withContext bufferedImage?.toComposeImageBitmap()
                }
                val input = URL(url).openStream()
                val bufferedImage = ImageIO.read(input)
                bufferedImage?.let {
                    // Cache to temporary file
                    ImageIO.write(bufferedImage, "png", cacheFile)
                    it.toComposeImageBitmap()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error loading image from URL: $url, ${e.message}")
            null
        }
    }
}

fun String?.isValidMediaUrl(): Boolean {
    if (this.isNullOrBlank()) return false
    return try {
        val uri = URI(this)
        if (uri.scheme == null) {
            // Check if it’s a valid local file
            File(this).exists()
        } else {
            // Check for valid schemes
            uri.scheme in listOf("file", "http", "https")
        }
    } catch (e: Exception) {
        // Invalid URI syntax
        false
    }
}


fun File.isValidAudioFile(): Boolean {
    val validExtensions = listOf("mp3", "wav", "m4a")
    return exists() && isFile && extension.lowercase() in validExtensions
}

fun String?.isImageUrl() = this?.startsWith("http", ignoreCase = true) == true || this?.startsWith("https", ignoreCase = true) == true

fun File.isValidImageFile(): Boolean {
    val validExtensions = listOf("jpg", "jpeg", "png")
    return exists() && isFile && extension.lowercase() in validExtensions
}

// Function to normalize URLs (local or network)
fun String.toNormalizeMediaUrl(): String {
    return if (File(this).exists()) {
        File(this).toURI().toString() // Convert local file to file:// URI
    } else {
        this // Assume network URL
    }
}

fun Double.formatTime(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this.toLong()) % 60
    return String.format("%d:%02d", minutes, seconds)
}

@OptIn(ExperimentalSerializationApi::class)
fun String.jsonToData():  List<VideoData> {
    val jsonParser = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    val response =  jsonParser.decodeFromString<RedditResponse>(this)
    val videoData = response
        .data
        ?.posts
        ?.map { post ->
            val video = post.data?.secureMedia?.video
            val width = video?.width
            val height = video?.height
            val aspectRatio = if (width != null && height != null) {
                width.toFloat() / height.toFloat()
            } else {
                null
            }
            VideoData(
                id = post.data?.id.orEmpty(),
                mediaUri = video?.hlsUrl.orEmpty(),
                previewImageUri = post.data?.preview?.images?.firstOrNull()?.source?.url.orEmpty(),
                aspectRatio = aspectRatio
            )
        }
        ?.filter { videoData ->
            videoData.id.isNotBlank()
                    && videoData.mediaUri.isNotBlank()
                    && videoData.previewImageUri.isNotBlank()
        }
        .orEmpty()
    return videoData
}