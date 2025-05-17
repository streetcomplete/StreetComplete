package de.westnordost.streetcomplete.util.ktx

import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

/**
 * Delete the given path, recursively if it is a directory.
 *
 * @throws kotlinx.io.files.FileNotFoundException - when [path] does not exist and [mustExist] is
 *         `true`
 * @throws kotlinx.io.IOException if there was an underlying error preventing listing the [path]'s
 *         children if it was a directory
 * */
fun FileSystem.deleteRecursively(path: Path, mustExist: Boolean = true) {
    val isDirectory = metadataOrNull(path)?.isDirectory ?: false
    if (isDirectory) {
        for (child in list(path)) {
            deleteRecursively(child, mustExist)
        }
    }
    delete(path, mustExist)
}
