package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
expect fun rememberShareFileLauncher(): (PlatformFile) -> Unit
