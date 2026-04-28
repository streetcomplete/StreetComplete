package de.westnordost.streetcomplete.quests

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.largeInput

abstract class ANameWithSuggestionsForm<T> : AbstractOsmQuestForm<T>() {

    @Composable
    override fun Content() {
        var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue())
        }

        QuestForm(
            answers = Confirm(
                isComplete = value.text.isNotEmpty(),
                onClick = { onClickOk(value.text) }
            )
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

    abstract val suggestions: List<String>?

    abstract fun onClickOk(value: String)
}
