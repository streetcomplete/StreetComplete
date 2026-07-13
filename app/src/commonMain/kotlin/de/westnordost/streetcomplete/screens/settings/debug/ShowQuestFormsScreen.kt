package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.no_search_results
import de.westnordost.streetcomplete.screens.main.bottom_sheet.quest.OsmQuestFormContainer
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import org.jetbrains.compose.resources.stringResource

/** Searchable and clickable quest list as a full screen */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowQuestFormsScreen(
    viewModel: ShowQuestFormsViewModel,
    onClickBack: () -> Unit,
) {
    val searchText by viewModel.searchText.collectAsState()
    val filteredQuests by viewModel.filteredQuests.collectAsState()
    var shownQuestType by remember { mutableStateOf<QuestType?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ShowQuestFormsTopAppBar(
                onClickBack = onClickBack,
                search = searchText,
                onSearchChange = viewModel::updateSearchText,
            )

            if (filteredQuests.isEmpty()) {
                CenteredLargeTitleHint(stringResource(Res.string.no_search_results))
            } else {
                val insets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                ).asPaddingValues()
                LazyQuestColumn(
                    items = filteredQuests,
                    onClickQuestType = { shownQuestType = it },
                    contentPadding = insets,
                    modifier = Modifier.consumeWindowInsets(insets)
                )
            }
        }

        (shownQuestType as? OsmElementQuestType<*>)?.let { questType ->
            OsmQuestFormContainer(
                onDismiss = { shownQuestType = null },
                onEdit = { action ->
                    when (action) {
                        is DeletePoiNodeAction -> {
                            message = "Deleted node"
                        }
                        is UpdateElementTagsAction -> {
                            val tagging = action.changes.changes.joinToString("\n")
                            message = "Tagging\n$tagging"
                        }
                    }
                    shownQuestType = null
                },
                onLeaveNote = {
                    message = "Leaving note"
                    shownQuestType = null
                },
                onHideQuest = {
                    message = "Hiding quest"
                    shownQuestType = null
                },
                onSplitWay = {
                    message = "Splitting way"
                    shownQuestType = null
                },
                onMoveNode = {
                    message = "Moving node"
                    shownQuestType = null
                },
                questType = questType,
                element = viewModel.mockElement,
                geometry = viewModel.mockGeometry,
                mapRotation = viewModel.mockRotation,
                mapTilt = 0f,
                mapMetersPerPixel = 0.01
            )
        }
    }

    message?.let { text ->
        InfoDialog(
            onDismissRequest = { message = null },
            text = { Text(text) }
        )
    }
}

