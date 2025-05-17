package de.westnordost.streetcomplete.data.osmnotes

import java.io.File

fun deleteImages(imagePaths: List<String>?) {
    for (path in imagePaths.orEmpty()) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
