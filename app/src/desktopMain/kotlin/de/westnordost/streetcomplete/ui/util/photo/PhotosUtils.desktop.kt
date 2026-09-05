package de.westnordost.streetcomplete.ui.util.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberPhotoCameraLauncher(
    onResult: (PlatformFile?) -> Unit,
): PhotoCameraLauncher = remember(onResult) {
    object : PhotoCameraLauncher {
        override fun launch(destinationFile: PlatformFile) {
            // TODO(multiplatform): Implement if FileKit adds desktop camera capture.
            onResult(null)
        }
    }
}

/** FileKit does not expose camera capture on desktop. Gallery/file picking remains available. */
@Composable
actual fun rememberHasCamera(): Boolean = false
