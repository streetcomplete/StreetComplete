package de.westnordost.streetcomplete.ui.util.photo

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings

@Composable @ReadOnlyComposable
actual fun createOpenCameraSettings() = FileKitOpenCameraSettings(
    authority = LocalContext.current.packageName + ".fileprovider"
)

@Composable
actual fun rememberHasCamera(): Boolean {
    val context = LocalContext.current
    return remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
}
