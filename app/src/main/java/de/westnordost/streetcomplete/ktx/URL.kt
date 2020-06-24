package de.westnordost.streetcomplete.ktx

import java.io.File
import java.net.URL

fun URL.saveToFile(file: File) {
    openStream().use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
