package de.westnordost.streetcomplete.util.ktx

import java.io.File

/** recursively delete all files in this directory */
fun File.purge() {
    for (file in listFiles()!!) {
        if (file.isDirectory) file.purge()
        file.delete()
    }
}
