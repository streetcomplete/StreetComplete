package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

interface FileShareLauncher {
    fun launch(file: PlatformFile)
}

@Composable
expect fun rememberFileShareLauncher(): FileShareLauncher
