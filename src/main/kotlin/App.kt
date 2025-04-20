import manual_di.ManualAppModule
import manual_di.ManualAppModuleImpl

class App {
    companion object {
        lateinit var appModule: ManualAppModule

        // Initialize the app module
        fun initializeModule() {
            if (::appModule.isInitialized) return
            appModule = ManualAppModuleImpl()
        }
    }
}