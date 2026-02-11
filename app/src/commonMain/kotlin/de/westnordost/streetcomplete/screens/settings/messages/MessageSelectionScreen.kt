package de.westnordost.streetcomplete.screens.settings.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.pref_title_message_achievements
import de.westnordost.streetcomplete.resources.pref_title_message_changelog
import de.westnordost.streetcomplete.resources.pref_title_message_osm_messages
import de.westnordost.streetcomplete.resources.pref_title_message_weekly_osm
import de.westnordost.streetcomplete.resources.pref_title_messages
import de.westnordost.streetcomplete.resources.pref_title_zoom_buttons
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.settings.Preference
import org.jetbrains.compose.resources.stringResource

/** Screen in which to select which message types should be shown to the user / which are supressed
 * */
@Composable
fun MessageSelectionScreen(
    viewModel: MessageSelectionViewModel,
    onClickBack: () -> Unit,
) {
    val disabledMessageTypes by viewModel.disabledMessageTypes.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.pref_title_messages)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )

        Column(Modifier
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ))
        ) {
            val showNewAchievements = Message.NewAchievement::class !in disabledMessageTypes
            Preference(
                name = stringResource(Res.string.pref_title_message_achievements),
                onClick = { viewModel.toggleDisableMessageType(Message.NewAchievement::class, showNewAchievements) },
            ) {
                Switch(checked = showNewAchievements, onCheckedChange = null)
            }

            val showChangelog = Message.NewVersion::class !in disabledMessageTypes
            Preference(
                name = stringResource(Res.string.pref_title_message_changelog),
                onClick = { viewModel.toggleDisableMessageType(Message.NewVersion::class, showChangelog) },
            ) {
                Switch(checked = showChangelog, onCheckedChange = null)
            }

            val showNewWeeklyOsm = Message.NewWeeklyOsm::class !in disabledMessageTypes
            Preference(
                name = stringResource(Res.string.pref_title_message_weekly_osm),
                onClick = { viewModel.toggleDisableMessageType(Message.NewWeeklyOsm::class, showNewWeeklyOsm) },
            ) {
                Switch(checked = showNewWeeklyOsm, onCheckedChange = null)
            }
        }
    }
}
