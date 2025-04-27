
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.HomeScreen
import java.awt.Dimension


fun main() = application {
    App.initializeModule()
    val windowState = rememberWindowState(
        size = DpSize(480.dp, 700.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("images/icon.ico"),
        title = "Music Player",
        state = windowState,
    ) {
        window.minimumSize = Dimension(480, 700)

//        val scope = rememberCoroutineScope()
//        scope.launch(Dispatchers.IO) {
//            App.appModule.redditDataRepository.fetchData().collect {
//                println("RedditData: $it")
//            }
//        }

        MaterialTheme {
            HomeScreen(windowState)
        }
    }
}
