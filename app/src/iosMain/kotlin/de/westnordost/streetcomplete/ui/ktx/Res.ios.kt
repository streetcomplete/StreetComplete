package de.westnordost.streetcomplete.ui.ktx

import de.westnordost.streetcomplete.resources.Res
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager

actual fun Res.exists(path: String): Boolean =
    NSFileManager.defaultManager.fileExistsAtPath(
        NSBundle.mainBundle.resourcePath + "/compose-resources/" + path
    )
