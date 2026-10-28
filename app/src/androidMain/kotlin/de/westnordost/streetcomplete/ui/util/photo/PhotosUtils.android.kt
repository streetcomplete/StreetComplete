package de.westnordost.streetcomplete.ui.util.photo

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberTakePhotoLauncher(onResult: (PlatformFile?) -> Unit): (PlatformFile) -> Unit {
    val settings = FileKitOpenCameraSettings(authority = LocalContext.current.packageName + ".fileprovider")
    val launcher = rememberCameraPickerLauncher(settings, onResult)
    return { file -> launcher.launch(destinationFile = file) }
}

@Composable
actual fun rememberHasCamera(): Boolean {
    val context = LocalContext.current
    return remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
}
