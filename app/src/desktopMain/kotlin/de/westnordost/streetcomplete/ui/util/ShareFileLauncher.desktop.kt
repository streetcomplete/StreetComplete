package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.awt.Desktop
import java.io.File

// TODO desktop: opens the file instead of sharing it, could use
//      https://github.com/NucleusFramework/Nucleus/tree/main/share for that later.
@Composable
actual fun rememberShareFileLauncher(): (PlatformFile) -> Unit = { file ->
    Desktop.getDesktop().open(File(file.path))
}
