package de.westnordost.streetcomplete.data.osmnotes

import java.io.File

import de.westnordost.streetcomplete.util.StreetCompleteImageUploader

object AttachPhotoUtils {
    fun uploadAndGetAttachedPhotosText(imageUploader: StreetCompleteImageUploader, imagePaths: List<String>?): String {
        if (imagePaths != null && imagePaths.isNotEmpty()) {
            val urls = imageUploader.upload(imagePaths)
            if (urls != null && urls.isNotEmpty()) {
                return "\n\nAttached photo(s):\n" + urls.joinToString("\n")
            }
        }
        return ""
    }

    fun deleteImages(imagePaths: List<String>?) {
        for (path in imagePaths.orEmpty()) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
