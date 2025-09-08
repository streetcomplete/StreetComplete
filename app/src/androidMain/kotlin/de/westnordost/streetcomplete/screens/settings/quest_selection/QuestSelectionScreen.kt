package de.westnordost.streetcomplete.screens.settings.quest_selection

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.no_search_results
import de.westnordost.streetcomplete.resources.quest_presets_default_name
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.util.ktx.displayRegion
import org.jetbrains.compose.resources.stringResource

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
@Composable
fun QuestSelectionScreen(
    viewModel: QuestSelectionViewModel,
    onClickBack: () -> Unit,
) {
    val currentPresetName by viewModel.selectedEditTypePresetName.collectAsState()

    val searchText by viewModel.searchText.collectAsStateWithLifecycle()

    val displayCountry = remember {
        viewModel.currentCountry?.let { getCountryName(it) } ?: "Atlantis"
    }

    val filteredQuests by viewModel.filteredQuests.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        QuestSelectionTopAppBar(
            currentPresetName = currentPresetName ?: stringResource(Res.string.quest_presets_default_name),
            onClickBack = onClickBack,
            onUnselectAll = { viewModel.unselectAll() },
            onReset = { viewModel.resetAll() },
            search = searchText,
            onSearchChange = viewModel::updateSearchText,
        )

        if (filteredQuests.isEmpty()) {
            CenteredLargeTitleHint(stringResource(Res.string.no_search_results))
        } else {
            val insets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ).asPaddingValues()

            QuestSelectionList(
                items = filteredQuests,
                displayCountry = displayCountry,
                onSelect = { questType, selected ->
                    viewModel.select(questType, selected)
                },
                onReorder = { questType, toAfter ->
                    viewModel.order(questType, toAfter)
                },
                modifier = Modifier.consumeWindowInsets(insets),
                contentPadding = insets,
            )
        }
    }
}

private fun getCountryName(countryCode: String): String =
    // we don't use the language, but we need it for correct construction of the languageTag
    Locale("en-$countryCode").displayRegion ?: countryCode
