import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerAnimation(
    isPlaying: Boolean,
    barCount: Int = 20, // Increased to 6 bars
    minHeight: Dp = 4.dp, // Minimum bar height
    maxHeight: Dp = 60.dp, // Maximum bar height
    modifier: Modifier = Modifier
) {
    // Infinite transition for continuous animation
    val infiniteTransition = rememberInfiniteTransition()

    // Define animation for each bar with different delays for staggered effect
    val barHeights = List(barCount) { index ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = minHeight.value,
                targetValue = maxHeight.value,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300,
                        easing = LinearEasing,
                        delayMillis = index * (200 / barCount) // Scale delay based on bar count
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            // When paused, hold at minimum height
            animateFloatAsState(targetValue = minHeight.value)
        }
    }

    Row(
        modifier = modifier
            .width((barCount * 8).dp) // 4.dp bar + 4.dp spacing per bar
            .height((maxHeight + 4.dp)) // Fit max height with padding
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        barHeights.forEach { animatedHeight ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.value.dp)
                    .background(Color(0xFF00B0FF), shape = RoundedCornerShape(2.dp))
            )
        }
    }
}