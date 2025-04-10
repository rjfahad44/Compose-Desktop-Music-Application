package models

import kotlinx.serialization.Serializable

@Serializable
data class MusicTrack(
    val title: String,
    val artist: String,
    val image: String?,
    val url: String,
    val isUserAdded: Boolean = false
)