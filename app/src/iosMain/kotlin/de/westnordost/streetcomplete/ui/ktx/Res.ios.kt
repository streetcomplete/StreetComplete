package de.westnordost.streetcomplete.ui.ktx

import de.westnordost.stretcomplete.resources.Res
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager

actual fun Res.exists(path: String): Boolean {
    val fileManager = NSFileManager.defaultManager
    val path = NSBundle.mainBundle.resourcePath + "/compose-resources/" + path
    return fileManager.fileExistsAtPath(path)
}
