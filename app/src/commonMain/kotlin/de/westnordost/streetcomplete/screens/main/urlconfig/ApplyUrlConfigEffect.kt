package de.westnordost.streetcomplete.screens.main.urlconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.urlconfig.UrlConfig

/** Offer to apply the given url config */
@Composable
fun ApplyUrlConfigEffect(
    urlConfig: UrlConfig,
    presetNameAlreadyExists: Boolean,
    onApplyUrlConfig: (urlConfig: UrlConfig) -> Unit
) {
    var showApplyUrlConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(urlConfig) { showApplyUrlConfigDialog = true }

    if (showApplyUrlConfigDialog) {
        ApplyUrlConfigDialog(
            onDismissRequest = { showApplyUrlConfigDialog = false },
            onConfirmed = { onApplyUrlConfig(urlConfig) },
            presetName = urlConfig.presetName,
            presetNameAlreadyExists = presetNameAlreadyExists
        )
    }
}
