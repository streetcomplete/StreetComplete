package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.auto_complete_text.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.theme.largeInput
import kotlin.collections.orEmpty

/** A quest form in which a name is entered with a list of suggestions to complete. E.g. user starts
 *  typing "Red" and is suggested "Red Cross". */
@Composable
fun NameWithSuggestionsQuestForm(
    on: (QuestAction<String>) -> Unit,
    suggestions: List<String>?,
    modifier: Modifier = Modifier,
    otherAnswers: @Composable (() -> List<AnswerItem>) = { emptyList() },
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val isTooLong by remember { derivedStateOf { name.text.length > MAX_OSM_TAG_VALUE_LENGTH } }

    QuestForm(
        on = on,
        isComplete = name.text.isNotEmpty() && !isTooLong,
        onClickOk = { on(Answer(name.text)) },
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        AutoCompleteTextField(
            value = name,
            onValueChange = { name = it },
            suggestions = suggestions
                ?.takeIf { name.text.length >= 3 }
                ?.filter { it.startsWith(name.text, ignoreCase = true) }
                .orEmpty(),
            textStyle = MaterialTheme.typography.largeInput,
            isError = isTooLong
        )
    }
}
