package models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val currentTrack: MusicTrack? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Double = 0.0,
    val duration: Double = 0.0,
    val currentTrackIndex: Int = -1
)