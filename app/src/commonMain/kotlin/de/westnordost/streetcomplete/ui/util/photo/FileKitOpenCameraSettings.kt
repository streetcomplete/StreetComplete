package de.westnordost.streetcomplete.ui.util.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings

@Composable @ReadOnlyComposable
expect fun createOpenCameraSettings(): FileKitOpenCameraSettings
