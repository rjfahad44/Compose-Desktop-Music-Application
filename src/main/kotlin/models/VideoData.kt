package models

import kotlinx.serialization.Serializable

@Serializable
data class VideoData(
    val id: String,
    val mediaUri: String,
    val previewImageUri: String,
    val aspectRatio: Float? = null
)
