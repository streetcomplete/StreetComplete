package de.westnordost.streetcomplete.ui.util.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType

@Composable @ReadOnlyComposable
actual fun createOpenCameraSettings() = FileKitOpenCameraSettings()

@Composable
actual fun rememberHasCamera(): Boolean = remember {
    UIImagePickerController.isSourceTypeAvailable(
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    )
}
