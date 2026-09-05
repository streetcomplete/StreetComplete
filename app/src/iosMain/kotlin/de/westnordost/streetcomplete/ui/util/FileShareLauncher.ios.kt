package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher

@Composable
actual fun rememberFileShareLauncher(): FileShareLauncher {
    val launcher = rememberShareFileLauncher()
    return remember(launcher) {
        object : FileShareLauncher {
            override fun launch(file: PlatformFile) {
                launcher.launch(file)
            }
        }
    }
}
