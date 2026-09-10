package de.westnordost.streetcomplete.ui.util.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType

@Composable
actual fun rememberPhotoCameraLauncher(
    onResult: (PlatformFile?) -> Unit,
): PhotoCameraLauncher {
    val launcher = rememberCameraPickerLauncher(FileKitOpenCameraSettings(), onResult)
    return remember(launcher) {
        object : PhotoCameraLauncher {
            override fun launch(destinationFile: PlatformFile) {
                launcher.launch(destinationFile = destinationFile)
            }
        }
    }
}

@Composable
actual fun rememberHasCamera(): Boolean = remember {
    UIImagePickerController.isSourceTypeAvailable(
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    )
}
