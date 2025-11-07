package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.AutoCompleteTextField
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.ui.util.content

abstract class ANameWithSuggestionsForm<T> : AbstractOsmQuestForm<T>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val textFieldValue: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue())
    protected val name: String get() = textFieldValue.value.text

    abstract val suggestions: List<String>?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AutoCompleteTextField(
                    value = textFieldValue.value,
                    onValueChange = {
                        textFieldValue.value = it
                        checkIsFormComplete()
                    },
                    suggestions = suggestions
                        ?.takeIf { name.length >= 3 }
                        ?.filter { it.startsWith(name, ignoreCase = true) }
                        .orEmpty(),
                    textStyle = MaterialTheme.typography.largeInput,
                )
            }
        } }
    }

    override fun isFormComplete() = textFieldValue.value.text.isNotEmpty()
}
