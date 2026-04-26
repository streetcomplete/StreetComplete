package de.westnordost.streetcomplete.quests.internet_access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddInternetAccessForm : AbstractOsmQuestForm<Set<InternetAccess>>() {

    @Composable
    override fun Content() {
        var selectedOptions by rememberSerializable { mutableStateOf(emptySet<InternetAccess>()) }

        QuestForm(
            answers = Confirm(isComplete = selectedOptions.isNotEmpty()) {
                applyAnswer(selectedOptions)
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_multiselect_hint))
                }
                CheckboxGroup(
                    options = InternetAccess.entries,
                    onSelectionChange = { option, selected ->
                        // "no" is exclusive
                        if (option == InternetAccess.NO && selected) {
                            selectedOptions = setOf(InternetAccess.NO)
                        } else {
                            selectedOptions =
                                if (selected) { selectedOptions + option }
                                else { selectedOptions - option }
                        }
                    },
                    selectedOptions = selectedOptions,
                    itemContent = { Text(stringResource(it.text)) }
                )
            }
        }
    }
}
