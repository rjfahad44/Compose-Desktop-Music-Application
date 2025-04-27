package video_player


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.Component
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayer(
    url: String,
    pagerState: PagerState,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    val mediaPlayerComponent = remember { initializeMediaPlayerComponent() }
    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer() }
    val factory = remember { { mediaPlayerComponent } }

    LaunchedEffect(pagerState) {
        mediaPlayer.media().startPaused(url)
    }

    LaunchedEffect(pagerState.settledPage, pageIndex) {
        if (pagerState.settledPage == pageIndex) {
            mediaPlayer.controls().play()
        } else {
            mediaPlayer.controls().pause()
        }
    }
    DisposableEffect(Unit) {
        onDispose{
            mediaPlayer?.release()
        }
    }
    SwingPanel(
        factory = factory,
        background = Color(0xFF121212),
        modifier = modifier
    )
}

private fun initializeMediaPlayerComponent(): Component {
    NativeDiscovery().discover()
    return if (isMacOS()) {
        CallbackMediaPlayerComponent()
    } else {
        EmbeddedMediaPlayerComponent()
    }
}

private fun isMacOS(): Boolean {
    val os = System
        .getProperty("os.name", "generic")
        .lowercase(Locale.ENGLISH)
    return "mac" in os || "darwin" in os
}

private fun Component.mediaPlayer() = when (this) {
    is CallbackMediaPlayerComponent -> mediaPlayer()
    is EmbeddedMediaPlayerComponent -> mediaPlayer()
    else -> error("mediaPlayer() can only be called on vlcj player components")
}