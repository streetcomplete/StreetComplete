package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberShareFileLauncher(): (PlatformFile) -> Unit = { file ->
    Desktop.getDesktop().open(File(file.path))
}
