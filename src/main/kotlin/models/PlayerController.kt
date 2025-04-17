package models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerController(
    val playPause: () -> Unit,
    val seekTo: (Float) -> Unit,
    val onSeekFinished: (Float) -> Unit,
    val previousTrack: () -> Unit,
    val nextTrack: () -> Unit,
    val playTrackAtIndex: (Int) -> Unit
)