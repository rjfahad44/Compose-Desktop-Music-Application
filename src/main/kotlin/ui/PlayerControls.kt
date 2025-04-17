package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.MusicTrack
import models.PlayerController
import util.formatTime

@Composable
fun PlayerControls(
    track: MusicTrack?,
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: Float,
    controller: PlayerController,
    duration: Double,
    currentPosition: Double
) {
    var isSeeking by remember { mutableStateOf(false) }
    var seekingProgress by remember { mutableFloatStateOf(progress) }

    Card(
        elevation = 8.dp,
        backgroundColor = Color(0xFF1A1A1A),
        modifier = Modifier.fillMaxWidth().padding(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 4.dp,
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPosition.formatTime(),
                    color = Color.White,
                    fontSize = 12.sp
                )

                Slider(
                    value = if (isSeeking) seekingProgress else progress,
                    onValueChange = { newProgress ->
                        isSeeking = true
                        seekingProgress = newProgress
                        controller.seekTo(newProgress)
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        controller.onSeekFinished(seekingProgress)
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFFBB86FC)
                    )
                )
                Text(
                    text = duration.formatTime(),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Track thumbnail with text
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    track?.let {
                        TrackThumbnail(
                            track = track,
                            modifier = Modifier.size(50.dp, 50.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                track.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(track.artist, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }


                // Player Controller button
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = controller.previousTrack) {
                        Icon(
                            painterResource("images/ic_previous.xml"),
                            "Previous",
                            tint = Color.White
                        )
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
                            IconButton(onClick = controller.playPause) {
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

                    IconButton(onClick = controller.nextTrack) {
                        Icon(painterResource("images/ic_next.xml"), "Next", tint = Color.White)
                    }
                }
            }
        }
    }
}