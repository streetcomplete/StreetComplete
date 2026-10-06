package de.westnordost.streetcomplete.ui.util.photo

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberHasCamera(): Boolean = false

@Composable
actual fun rememberTakePhotoLauncher(onResult: (PlatformFile?) -> Unit): (PlatformFile) -> Unit = {}
