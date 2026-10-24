package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import de.westnordost.streetcomplete.util.logs.Log
import dev.nucleusframework.application.LocalNucleusWindow
import dev.nucleusframework.application.NucleusWindow
import dev.nucleusframework.application.share
import dev.nucleusframework.share.ShareException
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberShareFileLauncher(): (PlatformFile) -> Unit {
    val window = LocalNucleusWindow.current
    val coroutineScope = rememberCoroutineScope()
    return remember(window, coroutineScope) {
        { file -> coroutineScope.launch { window.share(File(file.path)) } }
    }
}

private suspend fun NucleusWindow.share(file: File) {
    try {
        share { file(file.absolutePath) }
    } catch (e: ShareException) {
        // e.g. if there is no share sheet on this system, just open the file instead
        Log.w("ShareFileLauncher", "Unable to share ${file.name}", e)
        withContext(Dispatchers.IO) { Desktop.getDesktop().open(file) }
    }
}
