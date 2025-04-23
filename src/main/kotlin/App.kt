import manual_di.ManualAppModule
import manual_di.ManualAppModuleImpl
import java.io.File

class App {
    companion object {
        lateinit var appModule: ManualAppModule

        // Initialize the app module
        fun initializeModule() {
            // Set the path to the VLC library
            //System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC")
            //System.setProperty("vlcj.log", "DEBUG")

            val vlcLibPath = File("src/main/resources/vlc-libs").absolutePath
            System.setProperty("jna.library.path", vlcLibPath)
            System.setProperty("vlcj.log", "DEBUG") // Enable debug logging

            if (::appModule.isInitialized) return
            appModule = ManualAppModuleImpl()
        }
    }
}