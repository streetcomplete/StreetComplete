package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.util.dialogs.showProfileSelectionDialog
import org.koin.compose.koinInject

@Composable
fun QuickSettingsDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {

    val questPresetsController: QuestPresetsController = koinInject()
    val levelFilter: LevelFilter = koinInject()
    val prefs: Preferences = koinInject()
    val ctx = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuItem(onClick = {
            onDismissRequest()
            showProfileSelectionDialog(ctx, questPresetsController, prefs)
        })
        {
            Text(text = stringResource(R.string.quick_switch_preset))
        }
        DropdownMenuItem(
            onClick = {
                onDismissRequest()
                levelFilter.showLevelFilterDialog(ctx, viewModel.mapCamera.value)
            })
        {
            Text(text = stringResource(R.string.level_filter))
        }
        DropdownMenuItem(onClick = {
                onDismissRequest()
                prefs.prefs.putString(Prefs.THEME_BACKGROUND, if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP") "AERIAL" else "MAP")
            })
        {
            Text(text = stringResource(R.string.quick_switch_map_background))
        }
        DropdownMenuItem(onClick = {
            onDismissRequest()
            viewModel.reverseQuestOrder.value = !viewModel.reverseQuestOrder.value
        }) {
            val textResId = if (viewModel.reverseQuestOrder.collectAsState().value)
                R.string.quest_order_normal
            else R.string.quest_order_reverse
            Text(text = stringResource(textResId))
        }
    }
}
