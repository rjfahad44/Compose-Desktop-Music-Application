import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.MusicTrack
import util.loadImageFromFile

@Composable
fun TrackItem(track: MusicTrack, isSelected: Boolean, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isSelected) Color(0xFF3D3D3D) else Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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