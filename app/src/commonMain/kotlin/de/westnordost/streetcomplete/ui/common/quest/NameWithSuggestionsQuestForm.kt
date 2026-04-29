package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.orEmpty

/** A quest form in which a name is entered with a list of suggestions to complete. E.g. user starts
 *  typing "Red" and is suggested "Red Cross". */
@Composable
fun NameWithSuggestionsQuestForm(
    suggestions: List<String>?,
    onClickOk: (String) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: List<Answer> = emptyList(),
) {
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    QuestForm(
        answers = Confirm(
            isComplete = value.text.isNotEmpty(),
            onClick = { onClickOk(value.text) }
        ),
        modifier = modifier,
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
