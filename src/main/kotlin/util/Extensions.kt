package util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.MusicTrack
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO


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

fun Double.formatTime(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this.toLong()) % 60
    return String.format("%d:%02d", minutes, seconds)
}