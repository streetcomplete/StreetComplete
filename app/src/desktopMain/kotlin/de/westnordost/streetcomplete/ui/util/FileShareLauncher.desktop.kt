package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberFileShareLauncher(): FileShareLauncher = remember {
    object : FileShareLauncher {
        override fun launch(file: PlatformFile) {
            val desktop = Desktop.getDesktop()
            val localFile = File(file.path)
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(localFile)
            } else {
                // TODO(multiplatform): Use a native share sheet when Compose Desktop exposes one.
                error("Opening files is not supported by this desktop environment")
            }
        }
    }
}
