import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.*

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

fun getPlatform(): String {
    val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        osName.contains("win") -> "win"
        osName.contains("mac") -> "mac"
        osName.contains("linux") -> "linux"
        else -> throw GradleException("Unknown OS: $osName")
    }
}

val javafxVersion = "21.0.2"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)


    // JavaFX modules required
    implementation("org.openjfx:javafx-media:$javafxVersion:${getPlatform()}")
    implementation("org.openjfx:javafx-base:$javafxVersion:${getPlatform()}")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:${getPlatform()}")
    implementation("org.openjfx:javafx-controls:$javafxVersion:${getPlatform()}")
    implementation("org.openjfx:javafx-swing:$javafxVersion:${getPlatform()}")

    // VLCj
    implementation("uk.co.caprica:vlcj:4.8.0")

    // Ktor
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-okhttp:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    // slf4j
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")


    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MusicPlayer"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(file("src/main/resources/images/icon.ico"))
            }

            // Include native libraries in distribution
            includeAllModules = true
        }
    }
}


tasks.withType<JavaExec> {
    jvmArgs = listOf("--add-modules=javafx.controls,javafx.media,javafx.swing")
}
