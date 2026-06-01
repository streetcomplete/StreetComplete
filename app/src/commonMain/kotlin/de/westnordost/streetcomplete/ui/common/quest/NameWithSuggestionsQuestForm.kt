package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.theme.largeInput
import kotlin.collections.orEmpty

/** A quest form in which a name is entered with a list of suggestions to complete. E.g. user starts
 *  typing "Red" and is suggested "Red Cross". */
@Composable
fun NameWithSuggestionsQuestForm(
    suggestions: List<String>?,
    on: (QuestAction<String>) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: List<AnswerItem> = emptyList(),
) {
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    QuestForm(
        isComplete = value.text.isNotEmpty(),
        onClickOk = { on(Answer(value.text)) },
        on = on,
        modifier = modifier,
        otherAnswers = otherAnswers
    ) {
        AutoCompleteTextField(
            value = value,
            onValueChange = { value = it },
            suggestions = suggestions
                ?.takeIf { value.text.length >= 3 }
                ?.filter { it.startsWith(value.text, ignoreCase = true) }
                .orEmpty(),
            textStyle = MaterialTheme.typography.largeInput,
        )
    }
}
