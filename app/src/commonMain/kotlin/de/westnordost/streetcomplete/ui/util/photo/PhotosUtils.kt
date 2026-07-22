package de.westnordost.streetcomplete.ui.util.photo

import de.westnordost.streetcomplete.ApplicationConstants
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.write

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
