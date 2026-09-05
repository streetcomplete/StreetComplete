package de.westnordost.streetcomplete

import java.io.File
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

internal fun desktopFilesDirectory(): File {
    val packaged = System.getProperty("compose.application.resources.dir")
        ?.let(::File)
        ?.takeIf { it.isDirectory }
    if (packaged != null) return packaged

    return sequenceOf(
        File("app/src/commonMain/composeResources/files"),
        File("src/commonMain/composeResources/files"),
    ).map { it.absoluteFile }.firstOrNull { it.isDirectory }
        ?: error(
            "StreetComplete desktop resources were not prepared. " +
                "Run the app with the Gradle desktop run task.",
        )
}

internal fun desktopDataDirectory(): Path = createDesktopDirectory(
    windows = System.getenv("APPDATA")?.let { File(it, "StreetComplete") },
    macos = File(System.getProperty("user.home"), "Library/Application Support/StreetComplete"),
    linux = File(
        System.getenv("XDG_DATA_HOME") ?: File(System.getProperty("user.home"), ".local/share").path,
        "streetcomplete",
    ),
)

internal fun desktopCacheDirectory(): Path = createDesktopDirectory(
    windows = System.getenv("LOCALAPPDATA")?.let { File(it, "StreetComplete/Cache") },
    macos = File(System.getProperty("user.home"), "Library/Caches/StreetComplete"),
    linux = File(
        System.getenv("XDG_CACHE_HOME") ?: File(System.getProperty("user.home"), ".cache").path,
        "streetcomplete",
    ),
)

private fun createDesktopDirectory(windows: File?, macos: File, linux: File): Path {
    val os = System.getProperty("os.name").lowercase()
    val file = when {
        os.contains("win") -> windows ?: File(System.getProperty("user.home"), "StreetComplete")
        os.contains("mac") -> macos
        else -> linux
    }
    val path = Path(file.absolutePath)
    SystemFileSystem.createDirectories(path, mustCreate = false)
    return path
}
