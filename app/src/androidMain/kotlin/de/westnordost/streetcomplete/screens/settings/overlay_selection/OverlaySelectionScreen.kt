package de.westnordost.streetcomplete.screens.settings.overlay_selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_presets_default_name
import org.jetbrains.compose.resources.stringResource

/** Shows a screen in which the user can enable and disable overlays */
@Composable
fun OverlaySelectionScreen(
    viewModel: OverlaySelectionViewModel,
    onClickBack: () -> Unit,
) {
    val currentPresetName by viewModel.selectedEditTypePresetName.collectAsState()

    val overlays by viewModel.overlays.collectAsState()

    Column(Modifier.fillMaxSize()) {
        OverlaySelectionTopAppBar(
            currentPresetName = currentPresetName ?: stringResource(Res.string.quest_presets_default_name),
            onClickBack = onClickBack,
            onReset = { viewModel.resetAll() }
        )

        val insets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ).asPaddingValues()

        OverlaySelectionList(
            items = overlays,
            onSelect = { overlay, selected ->
                viewModel.select(overlay, selected)
            },
            modifier = Modifier.consumeWindowInsets(insets),
            contentPadding = insets,
        )
    }
}
