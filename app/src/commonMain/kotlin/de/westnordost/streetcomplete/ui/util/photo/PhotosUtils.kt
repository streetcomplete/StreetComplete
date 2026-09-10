package de.westnordost.streetcomplete.ui.util.photo

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.write
import kotlin.time.Clock

/** Lowers size and JPEG quality of the given photo file and overwrites the original */
suspend fun FileKit.compressPhotoAndOverwrite(file: PlatformFile) {
    val compressedImage = FileKit.compressImage(
        file = file,
        quality = ApplicationConstants.ATTACH_PHOTO_QUALITY,
        maxWidth = ApplicationConstants.ATTACH_PHOTO_MAX_SIZE,
        maxHeight = ApplicationConstants.ATTACH_PHOTO_MAX_SIZE,
    )
    file.write(compressedImage)
}

fun createPhotoPlatformFile(): PlatformFile {
    val time = Clock.System.now().toLocalDateTime().toString().replace(':', '-')
    // files dir and not cacheDir because we keep the photo on disk as long as the user didn't
    // upload the photo yet
    return PlatformFile(FileKit.filesDir, "photo_$time.jpg")
}

interface PhotoCameraLauncher {
    fun launch(destinationFile: PlatformFile)
}

@Composable
expect fun rememberPhotoCameraLauncher(onResult: (PlatformFile?) -> Unit): PhotoCameraLauncher

@Composable expect fun rememberHasCamera(): Boolean
