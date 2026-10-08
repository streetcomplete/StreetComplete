package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher as rememberFileKitShareFileLauncher

@Composable
actual fun rememberShareFileLauncher(): (PlatformFile) -> Unit {
    val launcher = rememberFileKitShareFileLauncher()
    return { file -> launcher.launch(file) }
}
